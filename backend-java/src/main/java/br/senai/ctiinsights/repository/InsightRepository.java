package br.senai.ctiinsights.repository;

import br.senai.ctiinsights.domain.Insight;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findAllByOrderByGeradoEmDesc();
}
