package com.example.ultimateredis.config;

import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/** Redis-based rate limiter using the token bucket algorithm */
@Component
public class RedisRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScriptExecutor scriptExecutor;

    public RedisRateLimiter(RedisTemplate<String, Object> redisTemplate, RedisScriptExecutor scriptExecutor) {
        this.redisTemplate = redisTemplate;
        this.scriptExecutor = scriptExecutor;
    }

    /**
     * Check if a request should be allowed based on rate limiting
     *
     * @param key The rate limiter key (e.g. user ID, IP address)
     * @param tokensPerSecond Number of operations allowed per second
     * @param burstCapacity Maximum burst size
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean tryAcquire(String key, double tokensPerSecond, int burstCapacity) {
        String rateLimiterKey = "rate-limiter:" + key;

        String script =
                """
                local tokens_key = KEYS[1]
                local timestamp_key = KEYS[2]

                local rate = tonumber(ARGV[1])
                local capacity = tonumber(ARGV[2])
                local now = tonumber(ARGV[3])
                local requested = tonumber(ARGV[4])

                local fill_time = capacity/rate
                local ttl = math.floor(fill_time*2)

                local last_tokens = tonumber(redis.call('get', tokens_key))
                if last_tokens == nil then
                    last_tokens = capacity
                end

                local last_refreshed = tonumber(redis.call('get', timestamp_key))
                if last_refreshed == nil then
                    last_refreshed = 0
                end

                local delta = math.max(0, now-last_refreshed)
                local filled_tokens = math.min(capacity, last_tokens+(delta*rate))
                local allowed = filled_tokens >= requested
                local new_tokens = filled_tokens
                if allowed then
                    new_tokens = filled_tokens - requested
                end

                redis.call('setex', tokens_key, ttl, new_tokens)
                redis.call('setex', timestamp_key, ttl, now)

                return allowed and 1 or 0
                """;

        List<String> keys = List.of(rateLimiterKey + ":tokens", rateLimiterKey + ":timestamp");

        Long currentMillis = System.currentTimeMillis();

        // Arguments: rate, capacity, current time, requested tokens
        return scriptExecutor.executeScript(
                script, Boolean.class, keys, tokensPerSecond, burstCapacity, currentMillis / 1000.0, 1);
    }

    /**
     * Create a rate limiter that allows a fixed number of operations in a time window
     *
     * @param key The rate limiter key (e.g. user ID, IP address)
     * @param maxOperations Maximum operations allowed in the window
     * @param windowDuration Time window duration
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean tryAcquireWithFixedWindow(String key, int maxOperations, Duration windowDuration) {
        String windowKey = "rate-limiter:fixed:" + key;

        Long currentCount = redisTemplate.opsForValue().increment(windowKey, 1);
        if (currentCount == 1) {
            // First request in window, set expiry
            redisTemplate.expire(windowKey, windowDuration);
        }

        // If count exceeds limit, deny request
        return currentCount <= maxOperations;
    }
}
