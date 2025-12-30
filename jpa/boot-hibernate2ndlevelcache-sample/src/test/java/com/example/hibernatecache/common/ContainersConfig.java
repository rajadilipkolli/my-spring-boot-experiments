package com.example.hibernatecache.common;

import com.redis.testcontainers.RedisContainer;
import java.io.FileWriter;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18.1-alpine"));
    }

    @Bean
    RedisContainer redisContainer() throws IOException {
        RedisContainer redisContainer =
                new RedisContainer(DockerImageName.parse("redis").withTag("8.4.0-alpine"));
        redisContainer.start();
        String ymlContent = """
                singleServerConfig:
                  address: "%s"
                """;
        String finalYml = ymlContent.formatted(redisContainer.getRedisURI());
        String resourcesPath = new ClassPathResource("").getURL().getPath();
        String yamlFilePath = resourcesPath + "redisson-test.yml";
        try (FileWriter writer = new FileWriter(yamlFilePath)) {
            writer.write(finalYml);
        }
        return redisContainer;
    }
}
