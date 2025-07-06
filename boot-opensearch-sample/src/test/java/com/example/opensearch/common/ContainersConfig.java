package com.example.opensearch.common;

import java.time.Duration;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    OpensearchContainer<?> opensearchContainer() {
        return new OpensearchContainer<>("opensearchproject/opensearch:3.1.0")
                .withStartupAttempts(5)
                .withStartupTimeout(Duration.ofMinutes(10));
    }
}
