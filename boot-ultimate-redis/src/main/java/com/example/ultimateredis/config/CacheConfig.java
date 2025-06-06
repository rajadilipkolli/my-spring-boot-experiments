package com.example.ultimateredis.config;

import com.example.ultimateredis.utils.AppConstants;
import io.lettuce.core.ReadFrom;
import java.time.Duration;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CacheConfigurationProperties.class)
@EnableCaching
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
    @Profile(AppConstants.PROFILE_SENTINEL)
    LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return clientConfigurationBuilder ->
                clientConfigurationBuilder.readFrom(ReadFrom.REPLICA_PREFERRED);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JdkSerializationRedisSerializer());
        template.setEnableTransactionSupport(true);
        return template;
    }

    @Override
    public CacheErrorHandler errorHandler() {

        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                // your custom error handling logic
            }

            @Override
            public void handleCachePutError(
                    RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
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
        };
    }
}
