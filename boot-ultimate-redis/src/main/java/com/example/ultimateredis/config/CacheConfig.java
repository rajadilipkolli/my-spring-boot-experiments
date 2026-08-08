package com.example.ultimateredis.config;

import com.example.ultimateredis.utils.AppConstants;
import io.lettuce.core.ReadFrom;
import java.time.Duration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({CacheConfigurationProperties.class, RedisProperties.class})
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public ApplicationListener<@NonNull ContextRefreshedEvent> redisCacheMigrationListener(
            RedisCacheManager redisCacheManager) {
        return event -> {
            try {
                redisCacheManager.resetCaches();
                log.info("Redis cache reset successfully during context refresh");
            } catch (Exception e) {
                log.warn("Failed to reset Redis cache during migration: {}", e.getMessage());
            }
        };
    }

    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            CacheConfigurationProperties cacheConfigurationProperties, RedisProperties redisProperties) {
        RedisCacheGZIPSerializer serializerGzip = new RedisCacheGZIPSerializer(redisProperties.getGzipThresholdBytes());
        return builder -> {
            builder.enableStatistics()
                    .cacheDefaults()
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializerGzip));
            cacheConfigurationProperties
                    .getCacheExpirations()
                    .forEach((cacheName, timeout) -> builder.withCacheConfiguration(
                            cacheName,
                            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(timeout))));
        };
    }

    @Bean
    @Profile(AppConstants.PROFILE_SENTINEL)
    LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer(
            RedisProperties redisProperties) {
        return clientConfigurationBuilder ->
                clientConfigurationBuilder.readFrom(ReadFrom.valueOf(redisProperties.getReadFrom()));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            LettuceConnectionFactory connectionFactory, JsonMapper jsonMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJacksonJsonRedisSerializer(jsonMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJacksonJsonRedisSerializer(jsonMapper));
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    @Override
    public CacheErrorHandler errorHandler() {

        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
                // Swallow read error to fall back to the source of truth
            }

            @Override
            public void handleCachePutError(
                    RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
                log.error("Cache put error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Cache evict error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Cache clear error in cache {}: {}", cache.getName(), exception.getMessage());
            }
        };
    }
}
