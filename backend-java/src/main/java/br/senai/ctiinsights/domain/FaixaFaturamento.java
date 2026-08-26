package br.senai.ctiinsights.domain;

import java.math.BigDecimal;

/** Faixas de faturamento anual usadas nos cortes do dashboard. */
public enum FaixaFaturamento {

    ATE_360K("Ate R$ 360 mil"),
    DE_360K_A_4_8M("R$ 360 mil a R$ 4,8 mi"),
    DE_4_8M_A_30M("R$ 4,8 mi a R$ 30 mi"),
    ACIMA_30M("Acima de R$ 30 mi"),
    NAO_INFORMADO("Nao informado");

    private static final BigDecimal LIMITE_MICRO = new BigDecimal("360000");
    private static final BigDecimal LIMITE_PEQUENA = new BigDecimal("4800000");
    private static final BigDecimal LIMITE_MEDIA = new BigDecimal("30000000");

    private final String descricao;

    FaixaFaturamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static FaixaFaturamento classificar(BigDecimal faturamentoAnual) {
        if (faturamentoAnual == null || faturamentoAnual.signum() <= 0) {
            return NAO_INFORMADO;
        }
        if (faturamentoAnual.compareTo(LIMITE_MICRO) <= 0) {
            return ATE_360K;
        }
        if (faturamentoAnual.compareTo(LIMITE_PEQUENA) <= 0) {
            return DE_360K_A_4_8M;
        }
        if (faturamentoAnual.compareTo(LIMITE_MEDIA) <= 0) {
            return DE_4_8M_A_30M;
        }
        return ACIMA_30M;
    }
}
