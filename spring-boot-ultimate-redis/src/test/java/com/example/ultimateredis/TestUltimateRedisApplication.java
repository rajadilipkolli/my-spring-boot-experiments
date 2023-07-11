package com.example.ultimateredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestUltimateRedisApplication {

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis").withTag("7.0.12"))
                .withExposedPorts(6379);
    }

    public static void main(String[] args) {
        SpringApplication.from(UltimateRedisApplication::main)
                .with(TestUltimateRedisApplication.class)
                .run(args);
    }
}
