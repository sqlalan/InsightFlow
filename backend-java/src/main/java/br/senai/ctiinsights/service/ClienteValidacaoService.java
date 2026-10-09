package br.senai.ctiinsights.service;

import br.senai.ctiinsights.dto.ClienteDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Validacao complementar do lote recebido em /api/clientes/validar.
 *
 * O DTO confere cada linha sozinha. O que depende de olhar a lista inteira
 * (codigo repetido) ou de alterar o conteudo (padronizar textos) fica aqui.
 * Nesta etapa nada e gravado: a lista preparada volta em JSON.
 */
@Service
public class ClienteValidacaoService {

    public List<ClienteDTO> validarEPreparar(List<ClienteDTO> clientes) {
        // Set nao aceita repeticao: add() devolve false quando o codigo ja apareceu.
        Set<String> codigos = new HashSet<>();
        for (ClienteDTO cliente : clientes) {
            String codigo = cliente.codigoCliente().trim();
            if (!codigos.add(codigo)) {
                // O GlobalExceptionHandler transforma em 400 com mensagem clara.
                throw new IllegalArgumentException("Código repetido na planilha: " + codigo);
            }
        }

        return clientes.stream()
                .map(this::padronizar)
                .toList();
    }

    /** Devolve um novo DTO com os textos limpos: record nao muda depois de criado. */
    private ClienteDTO padronizar(ClienteDTO c) {
        return new ClienteDTO(
                c.codigoCliente().trim(),
                c.nomeCliente().trim(),
                formatarNome(c.consultor()),
                padronizarSegmento(c.segmento()),
                c.nivelCliente().trim().toUpperCase(Locale.ROOT),
                c.faturamentoAnual(),
                c.servicosContratados().trim(),
                c.dataContratacao(),
                c.cidade() == null ? "" : c.cidade().trim(),
                c.uf() == null ? "" : c.uf().trim().toUpperCase(Locale.ROOT));
    }

    /** "IND.", "industria" e "Indústria" sao a mesma categoria: sai sempre "Indústria". */
    private String padronizarSegmento(String segmento) {
        String s = segmento.trim().toLowerCase(Locale.ROOT);
        if (s.equals("ind.") || s.equals("industria") || s.equals("indústria")) {
            return "Indústria";
        }
        if (s.equals("comercio") || s.equals("comércio")) {
            return "Comércio";
        }
        if (s.equals("servicos") || s.equals("serviços")) {
            return "Serviços";
        }
        return segmento.trim();
    }

    /**
     * "ANA SOUZA" vira "Ana Souza". A apostila deixa maiuscula so a primeira
     * letra do nome inteiro ("Ana souza"); aqui e a de cada palavra, igual ao
     * que o Pinia ja mostra na previa.
     */
    private String formatarNome(String nome) {
        StringBuilder resultado = new StringBuilder();
        for (String palavra : nome.trim().toLowerCase(Locale.ROOT).split("\\s+")) {
            if (!resultado.isEmpty()) {
                resultado.append(' ');
            }
            resultado.append(palavra.substring(0, 1).toUpperCase(Locale.ROOT)).append(palavra.substring(1));
        }
        return resultado.toString();
    }
}
