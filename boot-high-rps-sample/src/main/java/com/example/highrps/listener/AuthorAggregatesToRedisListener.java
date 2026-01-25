package com.example.highrps.listener;

import com.example.highrps.mapper.AuthorRequestToResponseMapper;
import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.model.response.AuthorResponse;
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
public class AuthorAggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorAggregatesToRedisListener.class);

    private final RedisTemplate<String, String> redis;
    private final AuthorRequestToResponseMapper mapper;
    private final String queueKey;
    private final JsonMapper jsonMapper;

    public AuthorAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            AuthorRequestToResponseMapper mapper,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper) {
        this.redis = redis;
        this.mapper = mapper;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(
            topics = "authors-aggregates",
            groupId = "authors-redis-writer",
            containerFactory = "authorKafkaListenerContainerFactory")
    public void handleAggregate(ConsumerRecord<String, AuthorRequest> record) {
        if (record == null) return;

        try {
            String key = record.key();
            AuthorRequest payload = record.value();
            log.debug(
                    "Received authors-aggregates record: partition={}, offset={}, key={}, valueIsNull={}",
                    record.partition(),
                    record.offset(),
                    key,
                    payload == null);

            String redisKey = "authors:" + key;

            // If payload is null -> tombstone: remove Redis key and enqueue delete marker
            if (payload == null) {
                try {
                    redis.opsForValue().getAndDelete(redisKey);
                } catch (Exception e) {
                    log.warn("Failed to delete redis key for tombstone: {}", redisKey, e);
                }
                try {
                    String tombstoneJson = jsonMapper.writeValueAsString(
                            Map.of("email", key, "__deleted", true, "__entity", "author"));
                    redis.opsForList().leftPush(queueKey, tombstoneJson);
                } catch (Exception e) {
                    log.error("Failed to enqueue tombstone marker for key: {}, may lose durability", key, e);
                }
                return;
            }

            // Map AuthorRequest to AuthorResponse for Redis storage
            AuthorResponse value = mapper.mapToAuthorResponse(payload);
            var json = AuthorResponse.toJson(value);

            String existing = redis.opsForValue().get(redisKey);

            // Idempotent: if the existing value matches desired value, skip write
            if (existing != null) {
                try {
                    AuthorResponse existingStats = AuthorResponse.fromJson(existing);
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
            log.error("Unhandled exception in handleAggregate for authors", e);
            throw e;
        }
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, AuthorRequest> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        AuthorRequest value = record.value();
        log.error("Received dead-letter message: key={}, value={} from topic {}", record.key(), value, topic);
        // Push failed message to a simple Redis DLQ list for later inspection
        String dlqKey = "dlq:authors-aggregates";
        try {
            if (value == null) {
                log.warn("DLT record has null value; key={}", record.key());
                return;
            }
            String payload = AuthorResponse.toJson(mapper.mapToAuthorResponse(value));
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ: {}", dlqKey, e);
        }
    }
}
