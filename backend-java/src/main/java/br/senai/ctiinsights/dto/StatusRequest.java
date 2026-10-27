package br.senai.ctiinsights.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** O admin so libera (ATIVO) ou bloqueia (BLOQUEADO); ninguem volta para PENDENTE. */
public record StatusRequest(
        @NotBlank(message = "informe o novo status")
        @Pattern(regexp = "ATIVO|BLOQUEADO", message = "o status deve ser ATIVO ou BLOQUEADO")
        String status) {
}
