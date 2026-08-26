package br.senai.ctiinsights.service;

import br.senai.ctiinsights.domain.Telemetria;
import br.senai.ctiinsights.repository.TelemetriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Grava a trilha de eventos de upload e de execucao do modulo Python. */
@Service
public class TelemetriaService {

    private static final int LIMITE_DETALHE = 500;

    private final TelemetriaRepository repository;

    public TelemetriaService(TelemetriaRepository repository) {
        this.repository = repository;
    }

    /**
     * Grava em transacao propria (REQUIRES_NEW).
     *
     * O registro de falha e feito de dentro do catch do ImportacaoService, que
     * roda em transacao: sem transacao separada, o rollback do upload apagaria
     * justamente o evento de erro que se queria guardar.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String evento, String status, String detalhe, long duracaoMs) {
        String texto = detalhe == null ? "" : detalhe;
        if (texto.length() > LIMITE_DETALHE) {
            texto = texto.substring(0, LIMITE_DETALHE);
        }
        repository.save(new Telemetria(evento, status, texto, duracaoMs));
    }

    public List<Telemetria> ultimosEventos() {
        return repository.findTop20ByOrderByTimestampDesc();
    }
}
