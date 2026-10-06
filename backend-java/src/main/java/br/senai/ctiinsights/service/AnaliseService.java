package br.senai.ctiinsights.service;

import br.senai.ctiinsights.config.AnalyticsProperties;
import br.senai.ctiinsights.exception.AnaliseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    /** Interpretador ja validado; evita repetir a sondagem a cada upload. */
    private volatile String interpretadorResolvido;

    public AnaliseService(AnalyticsProperties propriedades, ObjectMapper mapper) {
        this.propriedades = propriedades;
        this.mapper = mapper;
    }

    /** Roda o pipeline Python sobre a planilha e devolve o JSON de clientes tratados. */
    public ResultadoAnalise executar(Path planilha) {
        Path script = Path.of(propriedades.getPython().getScript()).toAbsolutePath().normalize();
        Path raizSaida = Path.of(propriedades.getPython().getOutput()).toAbsolutePath().normalize();

        if (!Files.exists(script)) {
            throw new AnaliseException("Script de analise nao encontrado em " + script);
        }

        long inicio = System.currentTimeMillis();
        Path saida = null;
        Process processo = null;
        try {
            Files.createDirectories(raizSaida);
            saida = Files.createTempDirectory(raizSaida, "execucao-");
            Path arquivoLog = saida.resolve("processo.log");
            processo = iniciar(script, planilha, saida, arquivoLog);
            boolean terminou = processo.waitFor(propriedades.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!terminou) {
                processo.destroyForcibly();
                throw new AnaliseException("O tratamento passou de "
                        + propriedades.getTimeoutSeconds() + " segundos e foi interrompido.");
            }
            if (processo.exitValue() != 0) {
                log.error("Modulo Python falhou (exit {}), consulte {}", processo.exitValue(), arquivoLog);
                throw new AnaliseException("O script de tratamento terminou com erro.");
            }
            long duracao = System.currentTimeMillis() - inicio;
            JsonNode clientes = lerJson(saida.resolve("clientes.json"));
            JsonNode insights = lerJson(saida.resolve("insights.json"));
            JsonNode indicadores = lerJson(saida.resolve("indicadores.json"));
            return new ResultadoAnalise(clientes, insights, indicadores, duracao,
                    Files.readString(arquivoLog, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AnaliseException("Falha ao executar o script de tratamento.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnaliseException("O tratamento foi interrompido.", e);
        } finally {
            if (processo != null && processo.isAlive()) {
                processo.descendants().forEach(ProcessHandle::destroyForcibly);
                processo.destroyForcibly();
            }
        }
    }

    /**
     * Interpretadores tentados, em ordem: primeiro o configurado, depois os
     * nomes usuais de cada sistema. No Windows o lancador costuma ser "py" e
     * "python" nem existe no PATH; no Linux do Render e "python3". Sem essa
     * lista, cada integrante do grupo precisaria descobrir e configurar o nome
     * certo antes de o upload funcionar na sua maquina.
     */
    private List<String> candidatos() {
        List<String> nomes = new ArrayList<>();
        nomes.add(propriedades.getPython().getExecutable());
        for (String padrao : List.of("python3", "python", "py")) {
            if (!nomes.contains(padrao)) {
                nomes.add(padrao);
            }
        }
        return nomes;
    }

    /**
     * Descobre o interpretador uma vez e guarda o resultado.
     *
     * Nao basta perguntar se o comando inicia: no Windows, "python" e "python3"
     * costumam ser atalhos da Microsoft Store que iniciam, imprimem um aviso e
     * saem com erro. Por isso cada candidato e testado com --version e so vale
     * se responder com codigo 0.
     */
    private String interpretador() {
        String jaResolvido = interpretadorResolvido;
        if (jaResolvido != null) {
            return jaResolvido;
        }
        for (String candidato : candidatos()) {
            if (responde(candidato)) {
                log.info("Modulo Python sera executado com '{}'", candidato);
                interpretadorResolvido = candidato;
                return candidato;
            }
        }
        throw new AnaliseException("Nenhum interpretador Python respondeu. Tentativas: " + candidatos()
                + ". Instale o Python ou defina PYTHON_BIN com o caminho do interpretador.");
    }

    private boolean responde(String comando) {
        Process teste = null;
        try {
            teste = new ProcessBuilder(comando, "--version").redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            return teste.waitFor(10, TimeUnit.SECONDS) && teste.exitValue() == 0;
        } catch (IOException e) {
            return false; // comando nao existe no PATH
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (teste != null && teste.isAlive()) teste.destroyForcibly();
        }
    }

    private Process iniciar(Path script, Path planilha, Path saida, Path arquivoLog) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                interpretador(),
                script.toString(),
                "--entrada", planilha.toAbsolutePath().toString(),
                "--saida", saida.toString());
        builder.directory(script.getParent().toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(arquivoLog.toFile());
        builder.environment().put("PYTHONIOENCODING", "utf-8");
        return builder.start();
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
