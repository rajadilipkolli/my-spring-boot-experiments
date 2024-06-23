package com.example.hibernatecache.common;

import java.io.FileWriter;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3-alpine"));
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() throws IOException {
        GenericContainer<?> redisContainer =
                new GenericContainer<>(DockerImageName.parse("redis").withTag("7.2.5-alpine")).withExposedPorts(6379);
        redisContainer.start();
        String ymlContent =
                """
                singleServerConfig:
                  address: "redis://%s:%d"
                """;
        String finalYml = ymlContent.formatted(redisContainer.getHost(), redisContainer.getMappedPort(6379));
        String resourcesPath = new ClassPathResource("").getURL().getPath();
        String yamlFilePath = resourcesPath + "redisson-test.yml";
        try (FileWriter writer = new FileWriter(yamlFilePath)) {
            writer.write(finalYml);
        }
        return redisContainer;
    }
}
