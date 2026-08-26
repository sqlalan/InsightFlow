package br.senai.ctiinsights.exception;

/** Codigo CTI repetido: o mesmo cliente ja existe na base. */
public class ClienteDuplicadoException extends RuntimeException {

    private final String codigoCti;

    public ClienteDuplicadoException(String codigoCti) {
        super("Ja existe um cliente cadastrado com o codigo CTI " + codigoCti);
        this.codigoCti = codigoCti;
    }

    public String getCodigoCti() {
        return codigoCti;
    }
}
