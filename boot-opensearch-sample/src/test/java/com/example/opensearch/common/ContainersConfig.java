package com.example.opensearch.common;

import java.time.Duration;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    OpenSearchContainer<?> opensearchContainer() {
        return new OpenSearchContainer<>("opensearchproject/opensearch:3.3.1")
                .withStartupAttempts(5)
                .withStartupTimeout(Duration.ofMinutes(10));
    }
}
