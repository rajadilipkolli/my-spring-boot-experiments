package com.example.opensearch.common;

import java.net.HttpURLConnection;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    static final GenericContainer<?> openSearchContainer =
            new GenericContainer<>("opensearchproject/opensearch:1.1.0")
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
                                    .withStartupTimeout(Duration.ofMinutes(4)));

    static {
        openSearchContainer.start();
    }

    @DynamicPropertySource
    static void setApplicationProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add(
                "opensearch.uris",
                () ->
                        "http://%s:%d"
                                .formatted(
                                        openSearchContainer.getHost(),
                                        openSearchContainer.getMappedPort(9200)));
    }
}
