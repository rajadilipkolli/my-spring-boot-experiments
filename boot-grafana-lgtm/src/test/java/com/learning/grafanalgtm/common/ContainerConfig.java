package com.learning.grafanalgtm.common;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

    @Bean
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.13.0"))
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(LgtmStackContainer lgtmContainer) {
        return registry -> {
            registry.add(
                    "management.opentelemetry.tracing.export.otlp.endpoint",
                    () -> lgtmContainer.getOtlpHttpUrl() + "/v1/traces");
            registry.add("management.otlp.metrics.export.url", () -> lgtmContainer.getOtlpHttpUrl() + "/v1/metrics");
            registry.add(
                    "management.opentelemetry.logging.export.otlp.endpoint",
                    () -> lgtmContainer.getOtlpHttpUrl() + "/v1/logs");
            registry.add("loki.uri", () -> lgtmContainer.getLokiUrl() + "/loki/api/v1/push");
        };
    }
}
