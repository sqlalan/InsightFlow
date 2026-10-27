package br.senai.ctiinsights.repository;

import br.senai.ctiinsights.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Mais recentes primeiro: os pedidos novos aparecem no topo da tela de aprovacao. */
    List<Usuario> findAllByOrderByCriadoEmDesc();
}
