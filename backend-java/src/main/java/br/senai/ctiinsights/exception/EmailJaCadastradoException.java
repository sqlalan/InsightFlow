package br.senai.ctiinsights.exception;

/** Ja existe conta com esse e-mail, inclusive pendente ou bloqueada: vira 409. */
public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Já existe uma conta com este e-mail. Se ela estiver bloqueada, fale com o administrador.");
    }
}
