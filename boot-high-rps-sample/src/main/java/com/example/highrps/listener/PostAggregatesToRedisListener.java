package com.example.highrps.listener;

import com.example.highrps.mapper.PostRequestToResponseMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PostAggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(PostAggregatesToRedisListener.class);

    private final RedisTemplate<String, String> redis;
    private final PostRequestToResponseMapper mapper;
    private final String queueKey;
    private final JsonMapper jsonMapper;

    public PostAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            PostRequestToResponseMapper mapper,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper) {
        this.redis = redis;
        this.mapper = mapper;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(
            topics = "posts-aggregates",
            groupId = "aggregates-redis-writer",
            containerFactory = "newPostKafkaListenerContainerFactory")
    public void handleAggregate(ConsumerRecord<String, NewPostRequest> record) {
        if (record == null) return;
        // Log record metadata early to diagnose tombstone timing issues
        try {
            String key = record.key();
            NewPostRequest payload = record.value();
            log.debug(
                    "Received posts-aggregates record: partition={}, offset={}, key={}, valueIsNull={}",
                    record.partition(),
                    record.offset(),
                    key,
                    payload == null);
            String redisKey = "posts:" + key;
            // If payload is null -> tombstone: remove Redis key and enqueue delete marker
            if (payload == null) {
                try {
                    redis.opsForValue().getAndDelete(redisKey);
                } catch (Exception e) {
                    log.warn("Failed to delete redis key for tombstone: {}", redisKey, e);
                }
                try {
                    String tombstoneJson =
                            jsonMapper.writeValueAsString(Map.of("title", key, "__deleted", true, "__entity", "post"));
                    redis.opsForList().leftPush(queueKey, tombstoneJson);
                } catch (Exception e) {
                    log.error("Failed to enqueue tombstone marker for key: {}, may lose durability", key, e);
                }
                return;
            }

            // Map NewPostRequest to PostResponse for Redis storage
            PostResponse value = mapper.mapToPostResponse(payload);
            var json = PostResponse.toJson(value);

            String existing = redis.opsForValue().get(redisKey);

            // Idempotent: if the existing value matches desired value, skip write
            if (existing != null) {
                try {
                    PostResponse existingStats = PostResponse.fromJson(existing);
                    if (existingStats.equals(value)) {
                        return;
                    }
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
        } catch (Exception e) {
            log.error("Unhandled exception in handleAggregate", e);
            throw e;
        }
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, NewPostRequest> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Received dead-letter message : {} from topic {}", record.value(), topic);
        // Push failed message to a simple Redis DLQ list for later inspection
        String dlqKey = "dlq:posts-aggregates";
        try {
            NewPostRequest postRequest = record.value();
            if (postRequest == null) {
                log.warn("DLT record has null value; key={}", record.key());
                return;
            }
            String payload = PostResponse.toJson(mapper.mapToPostResponse(postRequest));
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ: {}", dlqKey, e);
        }
    }
}
