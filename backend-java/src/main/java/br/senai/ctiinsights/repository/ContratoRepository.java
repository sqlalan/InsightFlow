package br.senai.ctiinsights.repository;

import br.senai.ctiinsights.domain.Contrato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    @Query("select s.nome, count(ct) from Contrato ct join ct.servico s "
            + "group by s.nome order by count(ct) desc")
    List<Object[]> contarPorServico();

    @Query("select function('to_char', ct.dataInicio, 'YYYY-MM'), count(ct) from Contrato ct "
            + "group by function('to_char', ct.dataInicio, 'YYYY-MM') "
            + "order by function('to_char', ct.dataInicio, 'YYYY-MM')")
    List<Object[]> evolucaoMensal();
}
