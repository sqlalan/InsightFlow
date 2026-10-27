package br.senai.ctiinsights.dto;

import br.senai.ctiinsights.domain.Usuario;
import java.time.LocalDateTime;

/** O que a tela de aprovacao ve de cada conta. O hash da senha nunca sai da API. */
public record UsuarioResponse(
        Long id, String nome, String email, String perfil, String status, LocalDateTime criadoEm) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(),
                u.getPerfil().name(), u.getStatus().name(), u.getCriadoEm());
    }
}
