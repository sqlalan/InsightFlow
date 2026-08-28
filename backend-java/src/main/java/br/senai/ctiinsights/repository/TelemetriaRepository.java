package br.senai.ctiinsights.repository;

import br.senai.ctiinsights.domain.Telemetria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {

    List<Telemetria> findTop20ByOrderByTimestampDesc();
}
