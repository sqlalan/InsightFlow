package br.senai.ctiinsights.service;

import br.senai.ctiinsights.domain.Cliente;
import br.senai.ctiinsights.domain.FaixaFaturamento;
import br.senai.ctiinsights.dto.ContagemResponse;
import br.senai.ctiinsights.dto.IndicadoresResponse;
import br.senai.ctiinsights.dto.InsightResponse;
import br.senai.ctiinsights.repository.ClienteRepository;
import br.senai.ctiinsights.repository.ContratoRepository;
import br.senai.ctiinsights.repository.InsightRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta os numeros do dashboard. As agregacoes rodam no banco (group by), nao em
 * memoria: e o mesmo raciocinio das consultas otimizadas vistas em Banco de Dados.
 */
@Service
public class DashboardService {

    private final ClienteRepository clientes;
    private final ContratoRepository contratos;
    private final InsightRepository insights;

    public DashboardService(ClienteRepository clientes, ContratoRepository contratos, InsightRepository insights) {
        this.clientes = clientes;
        this.contratos = contratos;
        this.insights = insights;
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse indicadores() {
        Double medio = clientes.faturamentoMedio();
        Double total = clientes.faturamentoTotal();
        return new IndicadoresResponse(
                clientes.count(),
                contratos.count(),
                total == null ? 0d : total,
                medio == null ? 0d : medio,
                converter(clientes.contarPorSegmento()),
                converter(clientes.contarPorNivel()),
                contarPorFaixa(),
                converter(contratos.contarPorServico()),
                converter(contratos.evolucaoMensal()));
    }

    @Transactional(readOnly = true)
    public List<InsightResponse> insights() {
        return insights.findAllByOrderByGeradoEmDesc().stream().map(InsightResponse::de).toList();
    }

    /**
     * A faixa de faturamento nao existe como coluna: ela e calculada por
     * Cliente.calcularFaixaFaturamento(). Agrupar aqui reaproveita essa regra em
     * vez de repetir os cortes em SQL.
     */
    private List<ContagemResponse> contarPorFaixa() {
        Map<FaixaFaturamento, Long> agrupado = clientes.findAll().stream()
                .collect(Collectors.groupingBy(Cliente::calcularFaixaFaturamento,
                        () -> new EnumMap<>(FaixaFaturamento.class), Collectors.counting()));
        return agrupado.entrySet().stream()
                .map(entrada -> new ContagemResponse(entrada.getKey().getDescricao(), entrada.getValue()))
                .toList();
    }

    /** Converte o resultado bruto do group by em pares rotulo/valor para o Chart.js. */
    private List<ContagemResponse> converter(List<Object[]> linhas) {
        return linhas.stream()
                .map(linha -> new ContagemResponse(
                        String.valueOf(linha[0]),
                        ((Number) linha[1]).longValue()))
                .toList();
    }
}
