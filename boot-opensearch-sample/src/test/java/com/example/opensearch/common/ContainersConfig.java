package com.example.opensearch.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public class ContainersConfig {

    static final GenericContainer<?> openSearchContainer =
            new GenericContainer<>("opensearchproject/opensearch:2.9.0")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("DISABLE_SECURITY_PLUGIN", "true")
                    .withEnv("OPENSEARCH_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withExposedPorts(9200, 9600);

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
