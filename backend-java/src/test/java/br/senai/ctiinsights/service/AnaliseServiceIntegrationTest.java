package br.senai.ctiinsights.service;

import static org.junit.jupiter.api.Assertions.*;
import br.senai.ctiinsights.config.AnalyticsProperties;
import br.senai.ctiinsights.exception.AnaliseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

@EnabledIfEnvironmentVariable(named = "INSIGHTFLOW_PYTHON_TEST", matches = ".+")
class AnaliseServiceIntegrationTest {
    @TempDir Path dir;

    @Test
    void interrompeProcessoQueNaoFechaSuaSaida() throws Exception {
        Path script = dir.resolve("lento.py");
        Files.writeString(script, "import time\ntime.sleep(30)\n");
        AnalyticsProperties properties = properties(script);
        properties.setTimeoutSeconds(1);
        AnaliseService service = new AnaliseService(properties, new ObjectMapper());
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            AnaliseException error = assertThrows(AnaliseException.class, () -> service.executar(dir.resolve("entrada.xlsx")));
            assertTrue(error.getMessage().contains("interrompido"));
        });
    }

    @Test
    void isolaArquivosDeCadaExecucao() throws Exception {
        Path script = dir.resolve("saida.py");
        Files.writeString(script, """
            import sys, pathlib
            out = pathlib.Path(sys.argv[sys.argv.index('--saida') + 1])
            for name in ['clientes', 'insights', 'indicadores']:
                (out / (name + '.json')).write_text('[]', encoding='utf-8')
            """);
        AnaliseService service = new AnaliseService(properties(script), new ObjectMapper());
        service.executar(dir.resolve("entrada.xlsx"));
        service.executar(dir.resolve("entrada.xlsx"));
        try (var paths = Files.list(dir.resolve("output"))) {
            assertEquals(2, paths.filter(Files::isDirectory).count());
        }
    }

    private AnalyticsProperties properties(Path script) {
        AnalyticsProperties properties = new AnalyticsProperties();
        properties.getPython().setExecutable(System.getenv("INSIGHTFLOW_PYTHON_TEST"));
        properties.getPython().setScript(script.toString());
        properties.getPython().setOutput(dir.resolve("output").toString());
        return properties;
    }
}
