package br.senai.ctiinsights.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente da carteira comercial da CTI.
 *
 * Encapsulamento: todos os atributos sao privados e so mudam por setters, o que
 * evita que segmento/nivel/faturamento sejam alterados de forma descontrolada.
 * Abstracao: quem usa a classe chama calcularFaixaFaturamento() sem conhecer os
 * limites de cada faixa.
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo interno da CTI: identificador de negocio, unico na base. */
    @Column(name = "codigo_cti", nullable = false, unique = true, length = 40)
    private String codigoCti;

    @Column(nullable = false, length = 80)
    private String segmento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private NivelCliente nivel;

    @Column(name = "faturamento_anual", precision = 15, scale = 2)
    private BigDecimal faturamentoAnual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultor_id")
    private Consultor consultor;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contrato> contratos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Insight> insights = new ArrayList<>();

    protected Cliente() {
        // exigido pelo JPA
    }

    public Cliente(String codigoCti, String segmento, NivelCliente nivel, BigDecimal faturamentoAnual) {
        this.codigoCti = codigoCti;
        this.segmento = segmento;
        this.nivel = nivel;
        this.faturamentoAnual = faturamentoAnual;
    }

    /** Abstracao: a regra de corte das faixas fica escondida em FaixaFaturamento. */
    public FaixaFaturamento calcularFaixaFaturamento() {
        return FaixaFaturamento.classificar(faturamentoAnual);
    }

    /** Contratos ainda vigentes deste cliente. */
    public long contratosAtivos() {
        return contratos.stream().filter(Contrato::estaAtivo).count();
    }

    public void adicionarContrato(Contrato contrato) {
        contratos.add(contrato);
        contrato.setCliente(this);
    }

    public void adicionarInsight(Insight insight) {
        insights.add(insight);
        insight.setCliente(this);
    }

    public Long getId() {
        return id;
    }

    public String getCodigoCti() {
        return codigoCti;
    }

    public void setCodigoCti(String codigoCti) {
        this.codigoCti = codigoCti;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public NivelCliente getNivel() {
        return nivel;
    }

    public void setNivel(NivelCliente nivel) {
        this.nivel = nivel;
    }

    public BigDecimal getFaturamentoAnual() {
        return faturamentoAnual;
    }

    public void setFaturamentoAnual(BigDecimal faturamentoAnual) {
        this.faturamentoAnual = faturamentoAnual;
    }

    public Consultor getConsultor() {
        return consultor;
    }

    public void setConsultor(Consultor consultor) {
        this.consultor = consultor;
    }

    public List<Contrato> getContratos() {
        return contratos;
    }

    public List<Insight> getInsights() {
        return insights;
    }
}
