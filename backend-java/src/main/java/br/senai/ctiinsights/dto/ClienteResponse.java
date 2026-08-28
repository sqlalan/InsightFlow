package br.senai.ctiinsights.dto;

import br.senai.ctiinsights.domain.Cliente;
import java.math.BigDecimal;

/** Cliente no formato que a tabela do dashboard consome. */
public record ClienteResponse(
        Long id,
        String codigoCti,
        String segmento,
        String nivel,
        BigDecimal faturamentoAnual,
        String faixaFaturamento,
        String consultor,
        long contratosAtivos) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getCodigoCti(),
                cliente.getSegmento(),
                cliente.getNivel().name(),
                cliente.getFaturamentoAnual(),
                cliente.calcularFaixaFaturamento().getDescricao(),
                cliente.getConsultor() == null ? null : cliente.getConsultor().getNome(),
                cliente.contratosAtivos());
    }
}
