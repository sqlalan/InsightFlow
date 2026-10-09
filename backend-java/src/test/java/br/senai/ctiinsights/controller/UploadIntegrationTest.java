package br.senai.ctiinsights.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"auth.admin-email=admin@example.com", "auth.admin-password=SenhaTeste123!",
        "analytics.python.executable=${INSIGHTFLOW_PYTHON_TEST:python}",
        "analytics.upload-dir=target/test-uploads", "analytics.python.output=target/test-analytics"})
@ActiveProfiles("local")
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "INSIGHTFLOW_PYTHON_TEST", matches = ".+")
class UploadIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void importarPlanilhaRealEReimportarSemDuplicarClientes() throws Exception {
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@example.com\",\"password\":\"SenhaTeste123!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = "Bearer " + mapper.readTree(login).path("token").asText();
        byte[] bytes = Files.readAllBytes(Path.of("../analytics-python/exemplos/CTI_Insights_modelo_upload_aula.xlsx"));
        MockMultipartFile file = new MockMultipartFile("arquivo", "modelo.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
        String result = mvc.perform(multipart("/api/planilhas").file(file).header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long count = mapper.readTree(result).path("clientesImportados").asLong();
        assertTrue(count > 0, result);
        mvc.perform(get("/api/indicadores").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalClientes").value(count));
        mvc.perform(multipart("/api/planilhas").file(file).header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.clientesImportados").value(0))
                .andExpect(jsonPath("$.clientesAtualizados").value(count));
        mvc.perform(get("/api/insights").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(get("/api/telemetria").header("Authorization", token)).andExpect(status().isOk());
    }
}
