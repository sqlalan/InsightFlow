package br.senai.ctiinsights.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/** Payload de criacao/atualizacao de cliente vindo do Front-end. */
public record ClienteRequest(
        @NotBlank(message = "informe o codigo CTI do cliente")
        String codigoCti,

        @NotBlank(message = "informe o segmento de atuacao")
        String segmento,

        @Pattern(regexp = "[ABCabc]", message = "o nivel deve ser A, B ou C")
        String nivel,

        @DecimalMin(value = "0.0", message = "o faturamento nao pode ser negativo")
        BigDecimal faturamentoAnual,

        String consultor) {
}
