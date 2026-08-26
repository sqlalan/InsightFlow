package br.senai.ctiinsights.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Parametros do modulo Python de analise, lidos de application.properties. */
@Component
@ConfigurationProperties(prefix = "analytics")
public class AnalyticsProperties {

    private final Python python = new Python();
    private String uploadDir = "uploads";
    private long timeoutSeconds = 120;

    public Python getPython() {
        return python;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public static class Python {
        private String executable = "python";
        private String script;
        private String output;

        public String getExecutable() {
            return executable;
        }

        public void setExecutable(String executable) {
            this.executable = executable;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }
}
