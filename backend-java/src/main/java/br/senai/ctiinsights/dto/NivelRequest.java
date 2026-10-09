package br.senai.ctiinsights.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Nivel obrigatorio para reclassificar um cliente existente. */
public record NivelRequest(
        @NotBlank(message = "informe o nivel do cliente")
        @Pattern(regexp = "[ABCabc]", message = "o nivel deve ser A, B ou C")
        String nivel) {
}
