package br.senai.ctiinsights.repository;

import br.senai.ctiinsights.domain.Consultor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorRepository extends JpaRepository<Consultor, Long> {

    Optional<Consultor> findByNomeIgnoreCase(String nome);
}
