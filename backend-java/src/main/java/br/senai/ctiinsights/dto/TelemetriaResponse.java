package br.senai.ctiinsights.dto;

import br.senai.ctiinsights.domain.Telemetria;
import java.time.LocalDateTime;

public record TelemetriaResponse(
        Long id, String evento, String status, String detalhe, Long duracaoMs, LocalDateTime timestamp) {

    public static TelemetriaResponse de(Telemetria t) {
        return new TelemetriaResponse(
                t.getId(), t.getEvento(), t.getStatus(), t.getDetalhe(), t.getDuracaoMs(), t.getTimestamp());
    }
}
