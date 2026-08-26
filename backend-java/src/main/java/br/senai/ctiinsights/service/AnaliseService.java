package br.senai.ctiinsights.service;

import br.senai.ctiinsights.config.AnalyticsProperties;
import br.senai.ctiinsights.exception.AnaliseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Ponte entre o Back-end Java e o modulo de Ciencia de Dados em Python.
 *
 * O Java nao reimplementa o tratamento: ele dispara o script com ProcessBuilder,
 * espera o processo terminar e le os arquivos JSON gerados em analytics-python/output.
 */
@Service
public class AnaliseService {

    private static final Logger log = LoggerFactory.getLogger(AnaliseService.class);

    private final AnalyticsProperties propriedades;
    private final ObjectMapper mapper;

    public AnaliseService(AnalyticsProperties propriedades, ObjectMapper mapper) {
        this.propriedades = propriedades;
        this.mapper = mapper;
    }

    /** Roda o pipeline Python sobre a planilha e devolve o JSON de clientes tratados. */
    public ResultadoAnalise executar(Path planilha) {
        Path script = Path.of(propriedades.getPython().getScript()).toAbsolutePath().normalize();
        Path saida = Path.of(propriedades.getPython().getOutput()).toAbsolutePath().normalize();

        if (!Files.exists(script)) {
            throw new AnaliseException("Script de analise nao encontrado em " + script);
        }

        ProcessBuilder builder = new ProcessBuilder(
                propriedades.getPython().getExecutable(),
                script.toString(),
                "--entrada", planilha.toAbsolutePath().toString(),
                "--saida", saida.toString());
        builder.directory(script.getParent().toFile());
        builder.redirectErrorStream(true);

        long inicio = System.currentTimeMillis();
        StringBuilder log0 = new StringBuilder();
        try {
            Process processo = builder.start();
            try (BufferedReader leitor = new BufferedReader(
                    new InputStreamReader(processo.getInputStream(), StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    log0.append(linha).append(System.lineSeparator());
                }
            }
            boolean terminou = processo.waitFor(propriedades.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!terminou) {
                processo.destroyForcibly();
                throw new AnaliseException("O tratamento passou de "
                        + propriedades.getTimeoutSeconds() + " segundos e foi interrompido.");
            }
            if (processo.exitValue() != 0) {
                log.error("Modulo Python falhou (exit {}):{}{}", processo.exitValue(), System.lineSeparator(), log0);
                throw new AnaliseException("O script de tratamento terminou com erro.");
            }
        } catch (IOException e) {
            throw new AnaliseException("Nao foi possivel iniciar o interpretador Python.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnaliseException("O tratamento foi interrompido.", e);
        }

        long duracao = System.currentTimeMillis() - inicio;
        JsonNode clientes = lerJson(saida.resolve("clientes.json"));
        JsonNode insights = lerJson(saida.resolve("insights.json"));
        JsonNode indicadores = lerJson(saida.resolve("indicadores.json"));
        return new ResultadoAnalise(clientes, insights, indicadores, duracao, log0.toString());
    }

    private JsonNode lerJson(Path arquivo) {
        if (!Files.exists(arquivo)) {
            throw new AnaliseException("O script nao gerou o arquivo " + arquivo.getFileName() + ".");
        }
        try {
            return mapper.readTree(Files.readString(arquivo, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AnaliseException("Saida do script em formato invalido: " + arquivo.getFileName(), e);
        }
    }

    /** Saida do pipeline Python ja desserializada. */
    public record ResultadoAnalise(
            JsonNode clientes, JsonNode insights, JsonNode indicadores, long duracaoMs, String logExecucao) {
    }
}
