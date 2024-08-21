package com.example.ultimateredis;

import com.redis.testcontainers.RedisClusterContainer;
import com.redis.testcontainers.RedisContainer;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestUltimateRedisApplication {

    @Bean
    @Profile("cluster")
    RedisClusterContainer redisClusterContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        RedisClusterContainer redisClusterContainer =
                new RedisClusterContainer(RedisClusterContainer.DEFAULT_IMAGE_NAME)
                        .withKeyspaceNotifications()
                        .withStartupAttempts(5)
                        .withStartupTimeout(Duration.ofMinutes(10));
        redisClusterContainer.start();
        List<String> list =
                Arrays.stream(redisClusterContainer.getRedisURIs())
                        .map(s -> s.substring(8))
                        .toList();
        dynamicPropertyRegistry.add("spring.data.redis.cluster.nodes", () -> list);
        return redisClusterContainer;
    }

    @Bean
    @ServiceConnection("redis")
    @Profile("!cluster")
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis").withTag("7.4.0-alpine"));
    }

    public static void main(String[] args) {
        SpringApplication.from(UltimateRedisApplication::main)
                .with(TestUltimateRedisApplication.class)
                .run(args);
    }
}
