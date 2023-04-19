package com.example.ultimateredis.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CacheConfigurationProperties.class)
@Slf4j
public class CacheConfig implements CachingConfigurer {

    private RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds));
    }

    @Bean
    @Primary
    public RedisCacheConfiguration defaultCacheConfig() {
        RedisCacheGZIPSerializer serializerGzip = new RedisCacheGZIPSerializer();

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializerGzip));
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            CacheConfigurationProperties properties) {
        log.info(
                "Redis (/Lettuce) configuration enabled. With cache timeout "
                        + properties.getTimeoutSeconds()
                        + " seconds.");

        RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(properties.getRedisHost());
        redisStandaloneConfiguration.setPort(properties.getRedisPort());
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration(CacheConfigurationProperties properties) {
        return createCacheConfiguration(properties.getTimeoutSeconds());
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            CacheConfigurationProperties properties) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        for (Entry<String, Long> cacheNameAndTimeout :
                properties.getCacheExpirations().entrySet()) {
            cacheConfigurations.put(
                    cacheNameAndTimeout.getKey(),
                    createCacheConfiguration(cacheNameAndTimeout.getValue()));
        }

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration(properties))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }

    private static class CustomCacheErrorHandler implements CacheErrorHandler {
        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            // your custom error handling logic
        }

        @Override
        public void handleCachePutError(
                RuntimeException exception, Cache cache, Object key, Object value) {
            // your custom error handling logic
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            // your custom error handling logic
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            // your custom error handling logic
        }
    }
}
