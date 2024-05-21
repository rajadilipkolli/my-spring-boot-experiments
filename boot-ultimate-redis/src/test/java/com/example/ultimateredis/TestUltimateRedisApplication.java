package com.example.ultimateredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestUltimateRedisApplication {

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        GenericContainer<?> redisContainer =
                new GenericContainer<>(DockerImageName.parse("redis").withTag("7.2.5-alpine"))
                        .withExposedPorts(6379);
        dynamicPropertyRegistry.add("cache.redis-port", () -> redisContainer.getMappedPort(6379));
        dynamicPropertyRegistry.add("cache.redis-host", redisContainer::getHost);
        return redisContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(UltimateRedisApplication::main)
                .with(TestUltimateRedisApplication.class)
                .run(args);
    }
}
