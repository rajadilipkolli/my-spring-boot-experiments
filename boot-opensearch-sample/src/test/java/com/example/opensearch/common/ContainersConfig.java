package com.example.opensearch.common;

import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class ContainersConfig {

    @Container
    public static OpensearchContainer<?> openSearchContainer =
            new OpensearchContainer<>(DockerImageName.parse("opensearchproject/opensearch:2.14.0"))
                    .withEnv("OPENSEARCH_INITIAL_ADMIN_PASSWORD", "admin");

    static {
        openSearchContainer.start();
    }

    @DynamicPropertySource
    static void setApplicationProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add(
                "opensearch.uris", () -> openSearchContainer.getHttpHostAddress());
    }
}
