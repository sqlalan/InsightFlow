package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.ClienteRequest;
import br.senai.ctiinsights.dto.ClienteResponse;
import br.senai.ctiinsights.service.ClienteService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD REST de clientes.
 *
 * Recurso como substantivo no plural (/api/clientes), verbo HTTP com o
 * significado correto e status coerente: 200 na leitura, 201 com Location na
 * criacao, 204 na remocao.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService servico;

    public ClienteController(ClienteService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String segmento) {
        return servico.listar(segmento);
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return servico.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest requisicao) {
        ClienteResponse criado = servico.criar(requisicao);
        return ResponseEntity.created(URI.create("/api/clientes/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ClienteResponse substituir(@PathVariable Long id, @Valid @RequestBody ClienteRequest requisicao) {
        return servico.atualizar(id, requisicao);
    }

    @PatchMapping("/{id}/nivel")
    public ClienteResponse reclassificar(@PathVariable Long id, @RequestBody Map<String, String> corpo) {
        return servico.atualizarNivel(id, corpo.get("nivel"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        servico.remover(id);
        return ResponseEntity.noContent().build();
    }
}
