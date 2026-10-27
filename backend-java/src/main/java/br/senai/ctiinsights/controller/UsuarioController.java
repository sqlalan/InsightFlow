package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.StatusRequest;
import br.senai.ctiinsights.dto.UsuarioResponse;
import br.senai.ctiinsights.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aprovacao de contas. So o ADMIN chega aqui: a regra fica no SecurityConfig
 * (hasRole("ADMIN")), nao na tela, porque qualquer um pode chamar a API direto.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService servico;

    public UsuarioController(UsuarioService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return servico.listar();
    }

    @PatchMapping("/{id}/status")
    public UsuarioResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest requisicao) {
        return servico.alterarStatus(id, requisicao.status());
    }
}
