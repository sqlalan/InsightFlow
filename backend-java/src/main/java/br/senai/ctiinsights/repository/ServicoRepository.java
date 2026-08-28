package br.senai.ctiinsights.repository;

import br.senai.ctiinsights.domain.Servico;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Optional<Servico> findByNomeIgnoreCase(String nome);
}
