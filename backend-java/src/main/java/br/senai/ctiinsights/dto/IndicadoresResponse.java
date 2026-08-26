package br.senai.ctiinsights.dto;

import java.util.List;

/** Tudo que a tela de dashboard precisa em uma unica chamada. */
public record IndicadoresResponse(
        long totalClientes,
        long totalContratos,
        double faturamentoTotal,
        double faturamentoMedio,
        List<ContagemResponse> clientesPorSegmento,
        List<ContagemResponse> clientesPorNivel,
        List<ContagemResponse> clientesPorFaixaFaturamento,
        List<ContagemResponse> contratosPorServico,
        List<ContagemResponse> evolucaoContratacoes) {
}
