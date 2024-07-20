package com.example.ultimateredis.config;

import io.lettuce.core.RedisURI;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CacheConfigurationProperties.class)
@EnableCaching
@Slf4j
public class CacheConfig implements CachingConfigurer {

    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            CacheConfigurationProperties cacheConfigurationProperties) {
        RedisCacheGZIPSerializer serializerGzip = new RedisCacheGZIPSerializer();
        return builder -> {
            builder.cacheDefaults()
                    .disableCachingNullValues()
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(
                                    serializerGzip));
            cacheConfigurationProperties
                    .getCacheExpirations()
                    .forEach(
                            (cacheName, timeout) ->
                                    builder.withCacheConfiguration(
                                            cacheName,
                                            RedisCacheConfiguration.defaultCacheConfig()
                                                    .entryTtl(Duration.ofSeconds(timeout))));
        };
    }

    @Bean
    LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer(
            CacheConfigurationProperties properties) {
        return clientConfigurationBuilder ->
                clientConfigurationBuilder.apply(RedisURI.create(properties.getRedisURI()));
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
