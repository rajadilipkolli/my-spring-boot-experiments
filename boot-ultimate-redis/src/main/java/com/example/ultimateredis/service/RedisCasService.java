package com.example.ultimateredis.service;

import com.example.ultimateredis.config.RedisProperties;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCasService {

    private static final Logger log = LoggerFactory.getLogger(RedisCasService.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisProperties redisProperties;

    public RedisCasService(RedisTemplate<String, Object> redisTemplate, RedisProperties redisProperties) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
    }

    private String buildKey(String rawKey) {
        return redisProperties.getKeyPrefix() + redisProperties.getKeyVersion() + rawKey;
    }

    public Boolean setIfEqual(String key, String value, String expectedValue) {
        String fullKey = buildKey(key);
        log.info("CAS setIfEqual key: {}", fullKey);
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object result = connection.execute(
                    "SET",
                    fullKey.getBytes(StandardCharsets.UTF_8),
                    value.getBytes(StandardCharsets.UTF_8),
                    "IFEQ".getBytes(StandardCharsets.UTF_8),
                    expectedValue.getBytes(StandardCharsets.UTF_8));
            return result != null;
        });
    }

    public Boolean setIfDoesNotEqual(String key, String value, String expectedValue) {
        String fullKey = buildKey(key);
        log.info("CAS setIfDoesNotEqual key: {}", fullKey);
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object result = connection.execute(
                    "SET",
                    fullKey.getBytes(StandardCharsets.UTF_8),
                    value.getBytes(StandardCharsets.UTF_8),
                    "IFDNE".getBytes(StandardCharsets.UTF_8),
                    expectedValue.getBytes(StandardCharsets.UTF_8));
            return result != null;
        });
    }

    public Long deleteExpected(String key, String expectedValue) {
        String fullKey = buildKey(key);
        log.info("CAS deleteExpected key: {}", fullKey);
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            return (Long) connection.execute(
                    "DELEX", fullKey.getBytes(StandardCharsets.UTF_8), expectedValue.getBytes(StandardCharsets.UTF_8));
        });
    }
}
