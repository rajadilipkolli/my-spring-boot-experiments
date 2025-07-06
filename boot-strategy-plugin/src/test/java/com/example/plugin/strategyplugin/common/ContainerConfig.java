package com.example.plugin.strategyplugin.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

    @Bean
    @ServiceConnection
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm").withTag("0.11.4"))
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(LgtmStackContainer lgtmContainer) {
        return registry -> {
            registry.add("management.otlp.tracing.endpoint", () -> lgtmContainer.getOtlpHttpUrl() + "/v1/traces");
            registry.add("loki.uri", () -> lgtmContainer.getLokiUrl() + "/loki/api/v1/push");
        };
    }
}
