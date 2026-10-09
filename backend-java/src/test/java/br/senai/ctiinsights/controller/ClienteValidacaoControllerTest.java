package br.senai.ctiinsights.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.senai.ctiinsights.exception.GlobalExceptionHandler;
import br.senai.ctiinsights.service.ClienteValidacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Os mesmos testes que a aula 10 faz no Thunder Client, automatizados.
 * addFilters = false desliga o login (Spring Security): aqui o assunto e a
 * validacao; o login tem os testes dele em ApiIntegrationTest.
 */
@WebMvcTest(ClienteValidacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ClienteValidacaoService.class, GlobalExceptionHandler.class})
class ClienteValidacaoControllerTest {

    @Autowired
    MockMvc mvc;

    private ResultActions enviar(String json) throws Exception {
        return mvc.perform(post("/api/clientes/validar").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void devolveListaPadronizadaQuandoTudoEstaValido() throws Exception {
        enviar("""
                [{"codigoCliente": " CTI001 ", "nomeCliente": "Metalúrgica Horizonte", "consultor": "ANA SOUZA",
                  "segmento": "IND.", "nivelCliente": "a", "faturamentoAnual": 1850000,
                  "servicosContratados": "Internet Dedicada;Firewall", "dataContratacao": "2025-01-15",
                  "cidade": "Campinas", "uf": "sp"}]""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoCliente").value("CTI001"))
                .andExpect(jsonPath("$[0].consultor").value("Ana Souza"))
                .andExpect(jsonPath("$[0].segmento").value("Indústria"))
                .andExpect(jsonPath("$[0].nivelCliente").value("A"))
                .andExpect(jsonPath("$[0].uf").value("SP"));
    }

    @Test
    void recusaCampoInvalidoInformandoCampoELinha() throws Exception {
        // Segundo item com faturamento negativo: o erro aponta o indice 1.
        enviar("""
                [{"codigoCliente": "CTI001", "nomeCliente": "A", "consultor": "Ana", "segmento": "Comercio",
                  "nivelCliente": "A", "faturamentoAnual": 10, "servicosContratados": "MPLS",
                  "dataContratacao": "2025-01-15"},
                 {"codigoCliente": "CTI002", "nomeCliente": "B", "consultor": "Carlos", "segmento": "Comercio",
                  "nivelCliente": "B", "faturamentoAnual": -10, "servicosContratados": "MPLS",
                  "dataContratacao": "2025-01-30"}]""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.campo").value("faturamentoAnual"))
                .andExpect(jsonPath("$.erro").value("O faturamento anual deve ser maior que zero"))
                .andExpect(jsonPath("$.indice").value("1"));
    }

    @Test
    void recusaCodigoRepetidoNoLote() throws Exception {
        String linha = """
                {"codigoCliente": "CTI001", "nomeCliente": "A", "consultor": "Ana", "segmento": "Comercio",
                 "nivelCliente": "A", "faturamentoAnual": 10, "servicosContratados": "MPLS",
                 "dataContratacao": "2025-01-15"}""";
        enviar("[" + linha + "," + linha + "]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Regra de validação não atendida"))
                .andExpect(jsonPath("$.erro").value("Código repetido na planilha: CTI001"));
    }
}
