package br.senai.ctiinsights.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Registro dos eventos de processamento: cada upload e cada execucao do script
 * Python deixam uma linha aqui. E a trilha que mostra o que aconteceu com uma
 * planilha sem precisar abrir log de servidor.
 */
@Entity
@Table(name = "telemetria")
public class Telemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String evento;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String detalhe;

    @Column(name = "duracao_ms")
    private Long duracaoMs;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    protected Telemetria() {
        // exigido pelo JPA
    }

    public Telemetria(String evento, String status, String detalhe, Long duracaoMs) {
        this.evento = evento;
        this.status = status;
        this.detalhe = detalhe;
        this.duracaoMs = duracaoMs;
    }

    public Long getId() {
        return id;
    }

    public String getEvento() {
        return evento;
    }

    public String getStatus() {
        return status;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public Long getDuracaoMs() {
        return duracaoMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
