package br.senai.ctiinsights.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

/** Payload de criacao/atualizacao de cliente vindo do Front-end. */
public record ClienteRequest(
        @NotBlank(message = "informe o codigo CTI do cliente")
        @Size(max = 40, message = "o codigo CTI deve ter no maximo 40 caracteres")
        String codigoCti,

        @NotBlank(message = "informe o segmento de atuacao")
        @Size(max = 80, message = "o segmento deve ter no maximo 80 caracteres")
        String segmento,

        @Pattern(regexp = "[ABCabc]", message = "o nivel deve ser A, B ou C")
        String nivel,

        @DecimalMin(value = "0.0", message = "o faturamento nao pode ser negativo")
        @Digits(integer = 13, fraction = 2, message = "o faturamento deve ter ate 13 digitos inteiros e 2 decimais")
        BigDecimal faturamentoAnual,

        @Size(max = 255, message = "o consultor deve ter no maximo 255 caracteres")
        String consultor) {
}
