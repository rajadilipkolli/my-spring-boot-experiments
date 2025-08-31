package com.example.learning.common;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.6-alpine")).withReuse(true);
    }

    @Bean
    @ServiceConnection
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.11.8"))
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }
}
