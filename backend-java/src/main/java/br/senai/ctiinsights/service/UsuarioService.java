package br.senai.ctiinsights.service;

import br.senai.ctiinsights.domain.PerfilUsuario;
import br.senai.ctiinsights.domain.StatusUsuario;
import br.senai.ctiinsights.domain.Usuario;
import br.senai.ctiinsights.dto.UsuarioResponse;
import br.senai.ctiinsights.exception.RecursoNaoEncontradoException;
import br.senai.ctiinsights.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tela de aprovacao do admin: listar contas, liberar e bloquear. */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final AuthService auth;

    public UsuarioService(UsuarioRepository usuarios, AuthService auth) {
        this.usuarios = usuarios;
        this.auth = auth;
    }

    public List<UsuarioResponse> listar() {
        return usuarios.findAllByOrderByCriadoEmDesc().stream().map(UsuarioResponse::de).toList();
    }

    /**
     * ATIVO aprova o pedido ou desbloqueia; BLOQUEADO recusa o pedido ou tira o
     * acesso. A conta do admin fica de fora, senao ele poderia se trancar fora.
     */
    @Transactional
    public UsuarioResponse alterarStatus(Long id, String novoStatus) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
        if (usuario.getPerfil() == PerfilUsuario.ADMIN) {
            throw new IllegalArgumentException("A conta do administrador não pode ser bloqueada.");
        }
        StatusUsuario status = StatusUsuario.valueOf(novoStatus);
        usuario.setStatus(status);
        if (status == StatusUsuario.BLOQUEADO) {
            auth.encerrarSessoes(usuario.getEmail());
        }
        return UsuarioResponse.de(usuario);
    }
}
