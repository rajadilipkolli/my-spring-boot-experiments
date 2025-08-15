package com.example.ultimateredis.config;

import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/** Utility class for executing Lua scripts against Redis */
@Component
public class RedisScriptExecutor {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisScriptExecutor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Execute a Lua script with keys and args, returning a result of type T
     *
     * @param <T> The return type of the script
     * @param script The Lua script to execute
     * @param keys List of keys the script will access
     * @param args Additional arguments to pass to the script
     * @return The result of script execution
     */
    public <T> T executeScript(String script, List<String> keys, Object... args) {
        RedisScript<T> redisScript = new DefaultRedisScript<>(script, (Class<T>) Object.class);
        return (T) redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * Execute a Lua script with explicit return type
     *
     * @param <T> The return type of the script
     * @param script The Lua script to execute
     * @param returnType The expected return type class
     * @param keys List of keys the script will access
     * @param args Additional arguments to pass to the script
     * @return The result of script execution
     */
    public <T> T executeScript(String script, Class<T> returnType, List<String> keys, Object... args) {
        RedisScript<T> redisScript = new DefaultRedisScript<>(script, returnType);
        return redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * Atomically increment a value within a range, wrapping around when it hits the max
     *
     * @param key The key to increment
     * @param min The minimum value (default 0)
     * @param max The maximum value (exclusive)
     * @return The new value after incrementing
     */
    public Long incrementWithinRange(String key, long min, long max) {
        String script =
                """
                local current = tonumber(redis.call('get', KEYS[1])) or ARGV[1] - 1
                current = current + 1
                if current >= tonumber(ARGV[2]) then
                    current = tonumber(ARGV[1])
                end
                redis.call('set', KEYS[1], current)
                return current
                """;
        return executeScript(script, Long.class, List.of(key), min, max);
    }

    /**
     * Set a value only if it doesn't exist or has expired
     *
     * @param key The key to set
     * @param value The value to set
     * @param ttlSeconds Time-to-live in seconds
     * @return true if set, false if already exists
     */
    public Boolean setIfNotExists(String key, String value, long ttlSeconds) {
        String script =
                """
                if redis.call('exists', KEYS[1]) == 0 then
                    redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2])
                    return 1
                else
                    return 0
                end
                """;
        return executeScript(script, Boolean.class, List.of(key), value, ttlSeconds);
    }
}
