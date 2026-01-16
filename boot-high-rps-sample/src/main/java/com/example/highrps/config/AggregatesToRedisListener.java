package com.example.highrps.config;

import com.example.highrps.model.response.PostResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class AggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(AggregatesToRedisListener.class);
    private final RedisTemplate<String, String> redis;
    private final String queueKey;

    public AggregatesToRedisListener(
            RedisTemplate<String, String> redis, @Value("${app.batch.queue-key:events:queue}") String queueKey) {
        this.redis = redis;
        this.queueKey = queueKey;
    }

    @KafkaListener(
            topics = "posts-aggregates",
            groupId = "aggregates-redis-writer",
            containerFactory = "stringKafkaListenerContainerFactory")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 500, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    public void handleAggregate(ConsumerRecord<String, String> record) {
        if (record == null) return;
        String key = record.key();
        String payload = record.value();
        if (key == null || payload == null || payload.isBlank()) return;
        PostResponse value;
        try {
            value = PostResponse.fromJson(payload);
        } catch (NumberFormatException nfe) {
            return;
        }

        String redisKey = "posts:" + key;
        String existing = redis.opsForValue().get(redisKey);
        var json = PostResponse.toJson(value);

        // Idempotent: if the existing value matches desired value, skip write
        if (existing != null) {
            try {
                PostResponse existingStats = PostResponse.fromJson(existing);
                if (existingStats.equals(value)) return;
            } catch (Exception e) {
                log.warn("Failed to parse existing stats for key: {}, will overwrite", key, e);
            }
        }

        // Write to Redis
        redis.opsForValue().set(redisKey, json);

        // Enqueue the same payload for asynchronous DB writes
        try {
            redis.opsForList().leftPush(queueKey, json);
        } catch (Exception e) {
            log.error("Failed to enqueue payload for DB write, key: {}, may lose durability", key, e);
        }
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, String> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Received dead-letter message : {} from topic {}", record.value(), topic);
        // Push failed message to a simple Redis DLQ list for later inspection
        String dlqKey = "dlq:posts-aggregates";
        try {
            String payload = record.value();
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ: {}", dlqKey, e);
        }
    }
}
