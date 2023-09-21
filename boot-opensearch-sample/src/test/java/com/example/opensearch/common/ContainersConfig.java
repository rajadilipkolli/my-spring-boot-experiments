package com.example.opensearch.common;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    static final GenericContainer<?> openSearchContainer =
            new GenericContainer<>("opensearchproject/opensearch:1.3.12")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("DISABLE_SECURITY_PLUGIN", "true")
                    .withEnv("OPENSEARCH_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withExposedPorts(9200, 9600);

    static {
        openSearchContainer.start();
        try {
            // waiting for container to start
            TimeUnit.SECONDS.sleep(30);
        } catch (Exception e) {
            // TODO: handle exception
        }
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
