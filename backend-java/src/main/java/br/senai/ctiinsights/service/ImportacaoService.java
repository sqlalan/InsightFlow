package br.senai.ctiinsights.service;

import br.senai.ctiinsights.config.AnalyticsProperties;
import br.senai.ctiinsights.domain.Cliente;
import br.senai.ctiinsights.domain.Consultor;
import br.senai.ctiinsights.domain.Contrato;
import br.senai.ctiinsights.domain.Insight;
import br.senai.ctiinsights.domain.InsightFaturamento;
import br.senai.ctiinsights.domain.InsightSegmento;
import br.senai.ctiinsights.domain.InsightServico;
import br.senai.ctiinsights.domain.NivelCliente;
import br.senai.ctiinsights.domain.Servico;
import br.senai.ctiinsights.domain.StatusContrato;
import br.senai.ctiinsights.dto.InsightResponse;
import br.senai.ctiinsights.dto.UploadResponse;
import br.senai.ctiinsights.repository.ClienteRepository;
import br.senai.ctiinsights.repository.ConsultorRepository;
import br.senai.ctiinsights.repository.InsightRepository;
import br.senai.ctiinsights.repository.ServicoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orquestra o caminho completo do upload:
 * validar a planilha -> rodar o Python -> gravar no PostgreSQL -> devolver os insights.
 */
@Service
public class ImportacaoService {

    private final PlanilhaService planilhas;
    private final AnaliseService analises;
    private final TelemetriaService telemetria;
    private final ClienteRepository clientes;
    private final ConsultorRepository consultores;
    private final ServicoRepository servicos;
    private final InsightRepository insights;
    private final AnalyticsProperties propriedades;

    public ImportacaoService(PlanilhaService planilhas, AnaliseService analises, TelemetriaService telemetria,
            ClienteRepository clientes, ConsultorRepository consultores, ServicoRepository servicos,
            InsightRepository insights, AnalyticsProperties propriedades) {
        this.planilhas = planilhas;
        this.analises = analises;
        this.telemetria = telemetria;
        this.clientes = clientes;
        this.consultores = consultores;
        this.servicos = servicos;
        this.insights = insights;
        this.propriedades = propriedades;
    }

    @Transactional
    public UploadResponse importar(MultipartFile arquivo) {
        long inicio = System.currentTimeMillis();
        Path destino = planilhas.salvar(arquivo, Path.of(propriedades.getUploadDir()));
        int linhas;
        try {
            linhas = planilhas.validar(destino);
        } catch (RuntimeException e) {
            telemetria.registrar("UPLOAD", "ERRO", e.getMessage(), System.currentTimeMillis() - inicio);
            throw e;
        }
        telemetria.registrar("UPLOAD", "OK", linhas + " linhas lidas",
                System.currentTimeMillis() - inicio);

        AnaliseService.ResultadoAnalise resultado;
        try {
            resultado = analises.executar(destino);
        } catch (RuntimeException e) {
            // O detalhe aparece na tela de Relatorios; a causa tecnica vai para o log.
            telemetria.registrar("ANALISE_PYTHON", "ERRO", "Análise não concluída",
                    System.currentTimeMillis() - inicio);
            throw e;
        }
        telemetria.registrar("ANALISE_PYTHON", "OK",
                resultado.clientes().size() + " clientes analisados", resultado.duracaoMs());

        Contadores contadores = gravarClientes(resultado.clientes());
        List<Insight> gerados = gravarInsights(resultado.insights());

        return new UploadResponse(
                destino.getFileName().toString(),
                linhas,
                contadores.criados,
                contadores.atualizados,
                contadores.contratos,
                System.currentTimeMillis() - inicio,
                gerados.stream().map(InsightResponse::de).toList(),
                contadores.avisos);
    }

    private Contadores gravarClientes(JsonNode tratados) {
        Contadores contadores = new Contadores();
        for (JsonNode linha : tratados) {
            String codigo = texto(linha, "codigo_cti");
            if (codigo.isBlank()) {
                contadores.avisos.add("Linha ignorada: cliente sem código.");
                continue;
            }
            Cliente cliente = clientes.findByCodigoCti(codigo).orElse(null);
            if (cliente == null) {
                cliente = new Cliente(codigo, texto(linha, "segmento"),
                        NivelCliente.of(texto(linha, "nivel")), decimal(linha, "faturamento_anual"));
                contadores.criados++;
            } else {
                cliente.setSegmento(texto(linha, "segmento"));
                cliente.setNivel(NivelCliente.of(texto(linha, "nivel")));
                cliente.setFaturamentoAnual(decimal(linha, "faturamento_anual"));
                contadores.atualizados++;
            }
            cliente.setConsultor(consultorDe(texto(linha, "consultor")));
            clientes.save(cliente);
            contadores.contratos += gravarContratos(cliente, linha, contadores);
        }
        return contadores;
    }

    private int gravarContratos(Cliente cliente, JsonNode linha, Contadores contadores) {
        JsonNode lista = linha.path("servicos_contratados");
        if (!lista.isArray() || lista.isEmpty()) {
            contadores.avisos.add("Cliente " + cliente.getCodigoCti() + " sem serviço informado.");
            return 0;
        }
        LocalDate inicio = data(linha, "data_inicio");
        StatusContrato status = StatusContrato.of(texto(linha, "status"));
        int novos = 0;
        for (JsonNode nome : lista) {
            String nomeServico = nome.asText("").trim();
            if (nomeServico.isEmpty()) {
                continue;
            }
            boolean jaContratado = cliente.getContratos().stream()
                    .anyMatch(contrato -> contrato.getServico().getNome().equalsIgnoreCase(nomeServico));
            if (jaContratado) {
                continue;
            }
            Servico servico = servicos.findByNomeIgnoreCase(nomeServico)
                    .orElseGet(() -> servicos.save(new Servico(nomeServico, texto(linha, "categoria_servico"))));
            cliente.adicionarContrato(new Contrato(inicio, status, servico));
            novos++;
        }
        return novos;
    }

    private List<Insight> gravarInsights(JsonNode lista) {
        List<Insight> gerados = new ArrayList<>();
        for (JsonNode item : lista) {
            String tipo = texto(item, "tipo").toUpperCase();
            String descricao = texto(item, "descricao");
            Insight insight = switch (tipo) {
                case "SEGMENTO" -> new InsightSegmento(descricao, texto(item, "segmento"),
                        numeroOuNulo(item, "participacao_percentual"));
                case "FATURAMENTO" -> new InsightFaturamento(descricao,
                        decimal(item, "faturamento_medio"), decimal(item, "faturamento_mediano"));
                case "SERVICO" -> new InsightServico(descricao, texto(item, "servico"),
                        inteiroOuNulo(item, "total_contratos"));
                default -> null;
            };
            if (insight != null) {
                gerados.add(insights.save(insight));
            }
        }
        return gerados;
    }

    private Consultor consultorDe(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return consultores.findByNomeIgnoreCase(nome)
                .orElseGet(() -> consultores.save(new Consultor(nome, null)));
    }

    private static String texto(JsonNode no, String campo) {
        return no.path(campo).asText("").trim();
    }

    private static BigDecimal decimal(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        return valor.isNumber() ? valor.decimalValue() : null;
    }

    /** null no JSON continua null: asDouble() devolveria 0.0 e o resumo diria "concentra 0,0%". */
    private static Double numeroOuNulo(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        return valor.isNumber() ? valor.asDouble() : null;
    }

    private static Integer inteiroOuNulo(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        return valor.isNumber() ? valor.asInt() : null;
    }

    private static LocalDate data(JsonNode no, String campo) {
        String bruto = texto(no, campo);
        if (bruto.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(bruto.substring(0, Math.min(10, bruto.length())));
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    /** Acumula o que foi gravado para montar o resumo da tela de upload. */
    private static final class Contadores {
        private int criados;
        private int atualizados;
        private int contratos;
        private final List<String> avisos = new ArrayList<>();
    }
}
