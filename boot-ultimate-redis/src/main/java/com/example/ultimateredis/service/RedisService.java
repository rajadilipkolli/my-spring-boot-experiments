package com.example.ultimateredis.service;

import com.example.ultimateredis.config.RedisValueOperationsUtil;
import com.example.ultimateredis.model.AddRedisRequest;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    private final RedisValueOperationsUtil<String> redisStringUtil;
    private final String keyPrefix;
    private final String keyVersion;

    public RedisService(
            RedisValueOperationsUtil<String> redisValueOpsUtil,
            @org.springframework.beans.factory.annotation.Value("${ultimate.redis.key-prefix:app:}") String keyPrefix,
            @org.springframework.beans.factory.annotation.Value("${ultimate.redis.key-version:v1:}")
                    String keyVersion) {
        this.redisStringUtil = redisValueOpsUtil;
        this.keyPrefix = keyPrefix;
        this.keyVersion = keyVersion;
    }

    private String buildKey(String rawKey) {
        return keyPrefix + keyVersion + rawKey;
    }

    @Retryable(
            includes = {RedisConnectionFailureException.class},
            maxRetries = 3)
    @ConcurrencyLimit(limit = 5)
    public void addRedis(AddRedisRequest request) {
        String key = buildKey(request.key());
        log.info("add redis {}", request);
        redisStringUtil.putValue(key, request.value());
        log.info("adding expiry for key {} as: {} minutes", key, request.expireMinutes());
        redisStringUtil.setExpire(key, request.expireMinutes(), TimeUnit.MINUTES);
    }

    @Retryable(
            includes = {RedisConnectionFailureException.class},
            maxRetries = 3)
    @ConcurrencyLimit(limit = 5)
    public String getValue(String key) {
        String fullKey = buildKey(key);
        log.info("get value {}", fullKey);
        return redisStringUtil.getValue(fullKey);
    }

    @Retryable(
            includes = {RedisConnectionFailureException.class},
            maxRetries = 3)
    @ConcurrencyLimit(limit = 5)
    public Set<String> getKeysByPattern(String pattern) {
        log.info("getting keys matching pattern: {}", pattern);
        return redisStringUtil.getKeysWithPattern(pattern);
    }

    @Retryable(
            includes = {RedisConnectionFailureException.class},
            maxRetries = 3)
    @ConcurrencyLimit(limit = 5)
    public void deleteByPattern(String pattern) {
        log.info("deleting keys matching pattern: {}", pattern);
        redisStringUtil.deleteByPattern(pattern);
    }

    public String digest(String key) {
        String fullKey = buildKey(key);
        return redisStringUtil.getRedisTemplate().execute((org.springframework.data.redis.core.RedisCallback<String>)
                connection -> {
                    byte[] result = (byte[])
                            connection.execute("DIGEST", fullKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    return result != null ? new String(result, java.nio.charset.StandardCharsets.UTF_8) : null;
                });
    }
}
