package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.ClienteDTO;
import br.senai.ctiinsights.service.ClienteValidacaoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe do Pinia as linhas que passaram na validacao da tela, valida de novo
 * e devolve a lista padronizada. Sem banco e sem Python nesta rota.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteValidacaoController {

    private final ClienteValidacaoService servico;

    public ClienteValidacaoController(ClienteValidacaoService servico) {
        this.servico = servico;
    }

    /** List<@Valid ClienteDTO>: o @Valid dentro do generic valida cada item da lista. */
    @PostMapping("/validar")
    public ResponseEntity<List<ClienteDTO>> validarClientes(@RequestBody List<@Valid ClienteDTO> clientes) {
        return ResponseEntity.ok(servico.validarEPreparar(clientes));
    }
}
