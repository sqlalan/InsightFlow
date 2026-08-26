package br.senai.ctiinsights.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Classe base dos insights estrategicos exibidos no dashboard.
 *
 * Heranca: InsightSegmento, InsightFaturamento e InsightServico estendem esta
 * classe. Polimorfismo: cada subtipo reescreve gerarResumo() e formata o texto
 * do insight do seu jeito, mas o servico trata todos como Insight.
 */
@Entity
@Table(name = "insight")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 400)
    private String descricao;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    protected Insight() {
        // exigido pelo JPA
    }

    protected Insight(String descricao) {
        this.descricao = descricao;
    }

    /** Rotulo curto do tipo, usado como titulo do card no dashboard. */
    public abstract String getTipo();

    /** Texto final do insight. Cada subtipo formata do seu jeito. */
    public abstract String gerarResumo();

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(LocalDateTime geradoEm) {
        this.geradoEm = geradoEm;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
