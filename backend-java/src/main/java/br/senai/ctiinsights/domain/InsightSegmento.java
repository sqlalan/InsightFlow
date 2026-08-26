package br.senai.ctiinsights.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Insight sobre concentracao da carteira por segmento de atuacao. */
@Entity
@DiscriminatorValue("SEGMENTO")
public class InsightSegmento extends Insight {

    @Column(length = 80)
    private String segmento;

    @Column(name = "participacao_percentual")
    private Double participacaoPercentual;

    protected InsightSegmento() {
        // exigido pelo JPA
    }

    public InsightSegmento(String descricao, String segmento, Double participacaoPercentual) {
        super(descricao);
        this.segmento = segmento;
        this.participacaoPercentual = participacaoPercentual;
    }

    @Override
    public String getTipo() {
        return "Segmento";
    }

    @Override
    public String gerarResumo() {
        if (participacaoPercentual == null) {
            return getDescricao();
        }
        return String.format("%s concentra %.1f%% da carteira. %s",
                segmento, participacaoPercentual, getDescricao());
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public Double getParticipacaoPercentual() {
        return participacaoPercentual;
    }

    public void setParticipacaoPercentual(Double participacaoPercentual) {
        this.participacaoPercentual = participacaoPercentual;
    }
}
