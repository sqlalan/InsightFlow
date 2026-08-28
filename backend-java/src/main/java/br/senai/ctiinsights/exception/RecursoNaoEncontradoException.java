package br.senai.ctiinsights.exception;

/** Recurso pedido por id nao existe: vira 404 para o Front-end. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " nao encontrado para o identificador " + id);
    }
}
