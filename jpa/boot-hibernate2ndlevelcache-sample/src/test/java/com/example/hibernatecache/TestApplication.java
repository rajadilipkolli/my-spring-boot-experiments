package com.example.hibernatecache;

import com.example.hibernatecache.common.ContainersConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers(ContainersConfig.class)
public class TestApplication {

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() throws IOException {
        GenericContainer<?> redisContainer =
                new GenericContainer<>(DockerImageName.parse("redis").withTag("7.2.5-alpine"))
                        .withExposedPorts(6379);
        redisContainer.start();
        String ymlContent =
                """
                singleServerConfig:
                  address: "redis://%s:%d"
                """;
        String finalYml =
                ymlContent.formatted(redisContainer.getHost(), redisContainer.getMappedPort(6379));
        String resourcesPath = new ClassPathResource("").getURL().getPath();
        String yamlFilePath = resourcesPath + "redisson-test.yml";
        try (FileWriter writer = new FileWriter(new File(yamlFilePath))) {
            writer.write(finalYml);
        }
        return redisContainer;
    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }
}
