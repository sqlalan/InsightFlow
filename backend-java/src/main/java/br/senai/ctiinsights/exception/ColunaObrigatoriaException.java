package br.senai.ctiinsights.exception;

import java.util.List;

/** Falta na planilha alguma coluna esperada (ex.: "segmento"). */
public class ColunaObrigatoriaException extends RuntimeException {

    private final List<String> colunasFaltantes;

    public ColunaObrigatoriaException(List<String> colunasFaltantes) {
        super("A planilha nao tem as colunas obrigatorias: " + String.join(", ", colunasFaltantes));
        this.colunasFaltantes = colunasFaltantes;
    }

    public List<String> getColunasFaltantes() {
        return colunasFaltantes;
    }
}
