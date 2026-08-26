package br.senai.ctiinsights.dto;

import br.senai.ctiinsights.domain.Insight;
import java.time.LocalDateTime;

/**
 * Insight pronto para o dashboard. O texto vem de gerarResumo(): o polimorfismo
 * decide o formato, o DTO so transporta o resultado.
 */
public record InsightResponse(Long id, String tipo, String resumo, LocalDateTime geradoEm) {

    public static InsightResponse de(Insight insight) {
        return new InsightResponse(
                insight.getId(),
                insight.getTipo(),
                insight.gerarResumo(),
                insight.getGeradoEm());
    }
}
