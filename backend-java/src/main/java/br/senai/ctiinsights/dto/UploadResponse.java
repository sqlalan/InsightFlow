package br.senai.ctiinsights.dto;

import java.util.List;

/** Resultado do envio da planilha, exibido na tela de upload. */
public record UploadResponse(
        String arquivo,
        int linhasLidas,
        int clientesImportados,
        int clientesAtualizados,
        int contratosImportados,
        long duracaoMs,
        List<InsightResponse> insights,
        List<String> avisos) {
}
