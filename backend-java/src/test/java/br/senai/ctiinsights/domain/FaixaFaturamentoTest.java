package br.senai.ctiinsights.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FaixaFaturamentoTest {

    @Test
    void classificaCadaFaixaPeloLimiteSuperior() {
        assertEquals(FaixaFaturamento.ATE_360K, FaixaFaturamento.classificar(new BigDecimal("360000")));
        assertEquals(FaixaFaturamento.DE_360K_A_4_8M, FaixaFaturamento.classificar(new BigDecimal("360001")));
        assertEquals(FaixaFaturamento.DE_360K_A_4_8M, FaixaFaturamento.classificar(new BigDecimal("4800000")));
        assertEquals(FaixaFaturamento.DE_4_8M_A_30M, FaixaFaturamento.classificar(new BigDecimal("30000000")));
        assertEquals(FaixaFaturamento.ACIMA_30M, FaixaFaturamento.classificar(new BigDecimal("30000001")));
    }

    @Test
    void faturamentoAusenteOuZeradoFicaSemFaixa() {
        assertEquals(FaixaFaturamento.NAO_INFORMADO, FaixaFaturamento.classificar(null));
        assertEquals(FaixaFaturamento.NAO_INFORMADO, FaixaFaturamento.classificar(BigDecimal.ZERO));
    }
}
