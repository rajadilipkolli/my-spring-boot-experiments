package com.example.highrps.infrastructure.cache;

import com.example.highrps.shared.config.AppProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
@EnableRedisRepositories(basePackages = "com.example.highrps")
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Creates the shared in-process cache with the configured entry limit and a five-minute write expiry.
     *
     * @param appProperties application configuration containing the local cache limit
     * @return the cache used by application read and write paths
     */
    @Bean
    Cache<String, String> localCache(AppProperties appProperties) {
        return Caffeine.newBuilder()
                .maximumSize(appProperties.getCache().getLocalMaxSize())
                .expireAfterWrite(Duration.ofMinutes(5))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .recordStats()
                .removalListener((key, value, cause) -> {
                    // Log or emit metrics for evictions
                    log.debug("Removed key: {}, cause: {}", key, cause);
                })
                .build();
    }

    /**
     * Creates the string Redis template used for cache reservations and deletion markers.
     *
     * @param factory the Redis connection factory
     * @return a template configured with string key and value serializers
     */
    @Bean
    @Primary
    RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        var serializer = new StringRedisSerializer();
        template.setConnectionFactory(factory);
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        return template;
    }
}
