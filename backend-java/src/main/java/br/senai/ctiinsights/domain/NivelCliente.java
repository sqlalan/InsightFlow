package br.senai.ctiinsights.domain;

/** Classificacao do cliente na carteira da CTI. Escala ordinal: A > B > C. */
public enum NivelCliente {
    A, B, C;

    public static NivelCliente of(String valor) {
        if (valor == null || valor.isBlank()) {
            return C;
        }
        return NivelCliente.valueOf(valor.trim().toUpperCase());
    }
}
