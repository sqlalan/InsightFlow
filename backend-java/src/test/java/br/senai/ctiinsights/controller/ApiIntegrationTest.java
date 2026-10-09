package br.senai.ctiinsights.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"auth.admin-email=admin@example.com", "auth.admin-password=SenhaTeste123!"})
@ActiveProfiles("local")
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String login() throws Exception {
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"admin@example.com","password":"SenhaTeste123!","remember":false}
                    """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.expiresAt").exists())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(json).path("token").asText();
    }

    @Test
    void autenticarProtegerERevogarSessao() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
        mvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("NAO_AUTENTICADO"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"admin@example.com","password":"errada123"}
                    """))
                .andExpect(status().isUnauthorized());
        String token = login();
        mvc.perform(get("/api/auth/me").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("admin@example.com"));
        mvc.perform(post("/api/auth/logout").header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/clientes").header("Authorization", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void crudValidacaoEDuplicidade() throws Exception {
        String token = login();
        String body = """
            {"codigoCti":" CTI-TESTE ","segmento":"Industria","nivel":"A",
             "faturamentoAnual":120000.50,"consultor":"Raphael"}
            """;
        String created = mvc.perform(post("/api/clientes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.codigoCti").value("CTI-TESTE"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).path("id").asLong();
        assertTrue(id > 0);
        mvc.perform(post("/api/clientes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        mvc.perform(get("/api/clientes/" + id).header("Authorization", token)).andExpect(status().isOk());
        for (String invalid : new String[]{"{}", "{\"nivel\":\"D\"}", "{\"nivel\":null}"}) {
            mvc.perform(patch("/api/clientes/" + id + "/nivel").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON).content(invalid)).andExpect(status().isBadRequest());
        }
        mvc.perform(patch("/api/clientes/" + id + "/nivel").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nivel\":\"b\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nivel").value("B"));
        mvc.perform(put("/api/clientes/" + id).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(get("/api/indicadores").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(post("/api/clientes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{invalido"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("DADOS_INVALIDOS"));
        mvc.perform(delete("/api/clientes/" + id).header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/clientes/" + id).header("Authorization", token)).andExpect(status().isNotFound());
    }

    @Test
    void corsPermiteReclassificacao() throws Exception {
        mvc.perform(options("/api/clientes/1/nivel").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
