package com.example.opensearch.common;

import java.net.HttpURLConnection;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection("elasticsearch")
    GenericContainer<?> createOpenSearchContainer() {
        return new GenericContainer<>("opensearchproject/opensearch:1.1.0")
                .withEnv("discovery.type", "single-node")
                .withEnv("DISABLE_SECURITY_PLUGIN", "true")
                .withEnv("OPENSEARCH_JAVA_OPTS", "-Xms512m -Xmx512m")
                .withExposedPorts(9200, 9600)
                .waitingFor(
                        new HttpWaitStrategy()
                                .forPort(9200)
                                .forStatusCodeMatching(
                                        response ->
                                                response == HttpURLConnection.HTTP_OK
                                                        || response
                                                                == HttpURLConnection
                                                                        .HTTP_UNAUTHORIZED)
                                .withStartupTimeout(Duration.ofMinutes(2)));
    }
}
