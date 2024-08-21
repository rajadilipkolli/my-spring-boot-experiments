package com.example.ultimateredis.common;

import com.example.ultimateredis.utils.AppConstants;
import com.redis.testcontainers.RedisClusterContainer;
import com.redis.testcontainers.RedisContainer;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @Profile(AppConstants.PROFILE_CLUSTER)
    RedisClusterContainer redisClusterContainer(DynamicPropertyRegistry dynamicPropertyRegistry) {
        RedisClusterContainer redisClusterContainer =
                new RedisClusterContainer(RedisClusterContainer.DEFAULT_IMAGE_NAME)
                        .withKeyspaceNotifications()
                        .withStartupAttempts(5)
                        .withStartupTimeout(Duration.ofMinutes(4));
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
    @Profile(AppConstants.PROFILE_NOT_CLUSTER)
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis").withTag("7.4.0-alpine"));
    }
}
