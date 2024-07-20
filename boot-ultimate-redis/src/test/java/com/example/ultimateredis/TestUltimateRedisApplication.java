package com.example.ultimateredis;

import com.redis.testcontainers.RedisContainer;
import java.time.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestUltimateRedisApplication {

    @Bean
    @ServiceConnection(name = "redis")
    RedisContainer redisContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        RedisContainer redisContainer =
                new RedisContainer(DockerImageName.parse("redis").withTag("7.2.5-alpine"))
                        .withStartupAttempts(5)
                        .withStartupTimeout(Duration.ofMinutes(10));
        dynamicPropertyRegistry.add("cache.redis-uri", redisContainer::getRedisURI);
        return redisContainer;
    }

    public static void main(String[] args) {
        SpringApplication.from(UltimateRedisApplication::main)
                .with(TestUltimateRedisApplication.class)
                .run(args);
    }
}
