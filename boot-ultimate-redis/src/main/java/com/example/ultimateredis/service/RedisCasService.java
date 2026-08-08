package com.example.ultimateredis.service;

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

    public RedisCasService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Boolean setIfEqual(String key, String value, String expectedValue) {
        log.info("CAS setIfEqual key: {}, expected: {}, new: {}", key, expectedValue, value);
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object result = connection.execute(
                    "SET",
                    key.getBytes(StandardCharsets.UTF_8),
                    value.getBytes(StandardCharsets.UTF_8),
                    "IFEQ".getBytes(StandardCharsets.UTF_8),
                    expectedValue.getBytes(StandardCharsets.UTF_8));
            return result != null;
        });
    }

    public Boolean setIfDoesNotEqual(String key, String value, String expectedValue) {
        log.info("CAS setIfDoesNotEqual key: {}, expected: {}, new: {}", key, expectedValue, value);
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object result = connection.execute(
                    "SET",
                    key.getBytes(StandardCharsets.UTF_8),
                    value.getBytes(StandardCharsets.UTF_8),
                    "IFNE".getBytes(StandardCharsets.UTF_8),
                    expectedValue.getBytes(StandardCharsets.UTF_8));
            return result != null;
        });
    }

    public Long deleteExpected(String key, String expectedValue) {
        log.info("CAS deleteExpected key: {}, expected: {}", key, expectedValue);
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            return (Long) connection.execute(
                    "DELEX", key.getBytes(StandardCharsets.UTF_8), expectedValue.getBytes(StandardCharsets.UTF_8));
        });
    }
}
