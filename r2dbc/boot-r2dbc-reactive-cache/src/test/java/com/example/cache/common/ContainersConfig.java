package com.example.cache.common;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres").withTag("18.0-alpine"));
    }

    @Bean
    @ServiceConnection("redis")
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis").withTag("8.2.2-alpine"));
    }
}
