package br.senai.ctiinsights.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** Consultor responsavel pelos clientes. Relacao 1 consultor -> N clientes. */
@Entity
@Table(name = "consultor")
public class Consultor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(unique = true)
    private String matricula;

    @OneToMany(mappedBy = "consultor", cascade = CascadeType.ALL)
    private List<Cliente> clientes = new ArrayList<>();

    protected Consultor() {
        // exigido pelo JPA
    }

    public Consultor(String nome, String matricula) {
        this.nome = nome;
        this.matricula = matricula;
    }

    /** Quantos clientes o consultor atende hoje. */
    public int totalClientes() {
        return clientes.size();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }
}
