package br.senai.ctiinsights.domain;

public enum StatusContrato {
    ATIVO, ENCERRADO, SUSPENSO;

    public static StatusContrato of(String valor) {
        if (valor == null || valor.isBlank()) {
            return ATIVO;
        }
        return StatusContrato.valueOf(valor.trim().toUpperCase());
    }
}
