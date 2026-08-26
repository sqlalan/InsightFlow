package br.senai.ctiinsights.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo padrao de erro devolvido ao Front-end. A mensagem e escrita para ser
 * exibida direto ao consultor da CTI, sem jargao de stack trace.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes) {

    public static ApiError of(int status, String erro, String mensagem) {
        return new ApiError(LocalDateTime.now(), status, erro, mensagem, List.of());
    }

    public static ApiError of(int status, String erro, String mensagem, List<String> detalhes) {
        return new ApiError(LocalDateTime.now(), status, erro, mensagem, detalhes);
    }
}
