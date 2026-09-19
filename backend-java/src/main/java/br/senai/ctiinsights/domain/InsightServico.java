package br.senai.ctiinsights.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Insight sobre os servicos mais contratados da carteira. */
@Entity
@DiscriminatorValue("SERVICO")
public class InsightServico extends Insight {

    @Column(name = "servico_nome", length = 120)
    private String servicoNome;

    @Column(name = "total_contratos")
    private Integer totalContratos;

    protected InsightServico() {
        // exigido pelo JPA
    }

    public InsightServico(String descricao, String servicoNome, Integer totalContratos) {
        super(descricao);
        this.servicoNome = servicoNome;
        this.totalContratos = totalContratos;
    }

    @Override
    public String getTipo() {
        return "Serviço";
    }

    @Override
    public String gerarResumo() {
        if (totalContratos == null) {
            return getDescricao();
        }
        return String.format("%s aparece em %d contratos. %s",
                servicoNome, totalContratos, getDescricao());
    }

    public String getServicoNome() {
        return servicoNome;
    }

    public void setServicoNome(String servicoNome) {
        this.servicoNome = servicoNome;
    }

    public Integer getTotalContratos() {
        return totalContratos;
    }

    public void setTotalContratos(Integer totalContratos) {
        this.totalContratos = totalContratos;
    }
}
