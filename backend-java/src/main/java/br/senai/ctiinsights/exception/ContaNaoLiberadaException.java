package br.senai.ctiinsights.exception;

/**
 * Senha certa, mas a conta esta PENDENTE ou BLOQUEADA: vira 403.
 * So e lancada depois de a senha conferir, para quem nao sabe a senha nao
 * conseguir descobrir quais e-mails tem conta.
 */
public class ContaNaoLiberadaException extends RuntimeException {

    private final String codigo;

    public ContaNaoLiberadaException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
