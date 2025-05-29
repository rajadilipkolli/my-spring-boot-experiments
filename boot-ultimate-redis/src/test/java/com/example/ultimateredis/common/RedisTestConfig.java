package com.example.ultimateredis.common;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Test configuration for unit tests that need a RedisTemplate but don't want to connect to a real
 * Redis instance.
 */
@TestConfiguration(proxyBeanMethods = false)
public class RedisTestConfig {

    @Bean
    @Primary
    public RedisConnectionFactory mockRedisConnectionFactory() {
        RedisConnectionFactory factory = Mockito.mock(RedisConnectionFactory.class);
        RedisConnection connection = Mockito.mock(RedisConnection.class);
        Mockito.when(factory.getConnection()).thenReturn(connection);
        return factory;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(mockRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> stringRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(mockRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
