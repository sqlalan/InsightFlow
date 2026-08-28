package br.senai.ctiinsights.repository;

import br.senai.ctiinsights.domain.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCodigoCti(String codigoCti);

    boolean existsByCodigoCti(String codigoCti);

    List<Cliente> findBySegmentoIgnoreCase(String segmento);

    @Query("select c.segmento, count(c) from Cliente c group by c.segmento order by count(c) desc")
    List<Object[]> contarPorSegmento();

    @Query("select c.nivel, count(c) from Cliente c group by c.nivel order by c.nivel")
    List<Object[]> contarPorNivel();

    @Query("select avg(c.faturamentoAnual) from Cliente c where c.faturamentoAnual is not null")
    Double faturamentoMedio();

    @Query("select coalesce(sum(c.faturamentoAnual), 0) from Cliente c")
    Double faturamentoTotal();
}
