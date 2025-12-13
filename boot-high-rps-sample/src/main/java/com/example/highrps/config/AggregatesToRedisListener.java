package com.example.highrps.config;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;

@Component
public class AggregatesToRedisListener {

    private final RedisTemplate<String, String> redis;

    public AggregatesToRedisListener(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @KafkaListener(topics = "stats-aggregates", groupId = "aggregates-redis-writer", containerFactory = "stringKafkaListenerContainerFactory")
    @Retryable(value = Exception.class, maxRetries = 4, delay = 500L, multiplier = 2.0)
    public void handleAggregate(String payload) {
        if (payload == null || payload.isBlank()) return;

        // payload expected as "key:value" or JSON depending on producer
        String[] parts = payload.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        String key = parts[0];
        Long value;
        try {
            value = Long.parseLong(parts[1]);
        } catch (NumberFormatException nfe) {
            return;
        }

        String redisKey = "stats:" + key;
        String existing = redis.opsForValue().get(redisKey);
        String desired = "{\"id\":\"" + key + "\",\"value\":" + value + "}";

        // Idempotent: if the existing value matches desired payload, skip write
        if (desired.equals(existing)) return;

        // Write to Redis
        redis.opsForValue().set(redisKey, desired);
    }

    @Recover
    public void recover(Exception e, String payload) {
        // Push failed message to a simple Redis DLQ list for later inspection
        try {
            String dlqKey = "dlq:stats-aggregates";
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception ignored) {
        }
    }
}
