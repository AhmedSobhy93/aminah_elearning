package com.aminah.elearning.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class OperationalReadinessConfigTest {

    @Test
    void productionProfileKeepsOperationsSettingsEnabled() throws IOException {
        String properties = resourceText("/application-production.properties");

        assertThat(properties).contains(
                "server.forward-headers-strategy=framework",
                "server.shutdown=graceful",
                "spring.lifecycle.timeout-per-shutdown-phase=30s",
                "management.endpoints.web.exposure.include=health",
                "management.endpoint.health.probes.enabled=true",
                "management.endpoint.health.show-details=never"
        );
    }

    @Test
    void deploymentDescriptorsUseReadinessHealthCheck() throws IOException {
        String render = fileText("render.yaml");
        String railway = fileText("railway.json");

        assertThat(render).contains("healthCheckPath: /actuator/health/readiness");
        assertThat(railway).contains("\"healthcheckPath\": \"/actuator/health/readiness\"");
    }

    private String resourceText(String resourcePath) throws IOException {
        return new String(
                getClass().getResourceAsStream(resourcePath).readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private String fileText(String path) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(path)), StandardCharsets.UTF_8);
    }
}
