package br.senai.ctiinsights.exception;

/** Falha na execucao do modulo Python de tratamento e analise. */
public class AnaliseException extends RuntimeException {

    public AnaliseException(String mensagem) {
        super(mensagem);
    }

    public AnaliseException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
