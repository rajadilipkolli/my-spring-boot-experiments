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
    @ServiceConnection("redis")
    GenericContainer<?> redisContainer() throws IOException {
        GenericContainer redisContiner =
                new GenericContainer<>(DockerImageName.parse("redis").withTag("7.2.5-alpine"))
                        .withExposedPorts(6379);
        redisContiner.start();
        String ymlContent =
                """
                singleServerConfig:
                  address: "redis://%s:%d"
                """;
        String formatted =
                ymlContent.formatted(redisContiner.getHost(), redisContiner.getMappedPort(6379));
        String resourcesPath = new ClassPathResource("").getURL().getPath();

        String yamlFilePath = resourcesPath + "redisson-test.yml";

        // Create the YML FIle
        File yamlFile = new File(yamlFilePath);
        FileWriter writer = new FileWriter(yamlFile);
        writer.write(formatted);
        writer.close();
        return redisContiner;
    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }
}
