package br.senai.ctiinsights.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Insight sobre a distribuicao de faturamento da carteira. */
@Entity
@DiscriminatorValue("FATURAMENTO")
public class InsightFaturamento extends Insight {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    @Column(name = "faturamento_medio", precision = 15, scale = 2)
    private BigDecimal faturamentoMedio;

    @Column(name = "faturamento_mediano", precision = 15, scale = 2)
    private BigDecimal faturamentoMediano;

    protected InsightFaturamento() {
        // exigido pelo JPA
    }

    public InsightFaturamento(String descricao, BigDecimal faturamentoMedio, BigDecimal faturamentoMediano) {
        super(descricao);
        this.faturamentoMedio = faturamentoMedio;
        this.faturamentoMediano = faturamentoMediano;
    }

    @Override
    public String getTipo() {
        return "Faturamento";
    }

    @Override
    public String gerarResumo() {
        if (faturamentoMedio == null || faturamentoMediano == null) {
            return getDescricao();
        }
        NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
        return String.format("Faturamento médio de %s e mediano de %s. %s",
                moeda.format(faturamentoMedio), moeda.format(faturamentoMediano), getDescricao());
    }

    public BigDecimal getFaturamentoMedio() {
        return faturamentoMedio;
    }

    public void setFaturamentoMedio(BigDecimal faturamentoMedio) {
        this.faturamentoMedio = faturamentoMedio;
    }

    public BigDecimal getFaturamentoMediano() {
        return faturamentoMediano;
    }

    public void setFaturamentoMediano(BigDecimal faturamentoMediano) {
        this.faturamentoMediano = faturamentoMediano;
    }
}
