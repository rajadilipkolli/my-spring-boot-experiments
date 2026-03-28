package com.example.highrps.author.listener;

import com.example.highrps.author.domain.AuthorRedis;
import com.example.highrps.author.domain.AuthorRedisRepository;
import com.example.highrps.author.dto.AuthorRequest;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import java.util.Map;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AuthorAggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorAggregatesToRedisListener.class);

    private final RedisTemplate<String, String> redis;
    private final String queueKey;
    private final JsonMapper jsonMapper;
    private final AuthorRedisRepository authorRedisRepository;
    private final DeletionMarkerHandler deletionMarkerHandler;

    public AuthorAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            AuthorRedisRepository authorRedisRepository,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.redis = redis;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
        this.authorRedisRepository = authorRedisRepository;
        this.deletionMarkerHandler = deletionMarkerHandler;
    }

    @KafkaListener(
            topics = "authors-aggregates",
            groupId = "authors-redis-writer",
            containerFactory = "authorKafkaListenerContainerFactory")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 500, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    public void handleAggregate(ConsumerRecord<String, byte[]> record) {
        try {
            String key = record.key();
            byte[] bytes = record.value();
            log.debug(
                    "Received authors-aggregates record: partition={}, offset={}, key={}, valueIsNull={}",
                    record.partition(),
                    record.offset(),
                    key,
                    bytes == null);
            // If payload is null -> tombstone: remove Redis key and enqueue delete marker
            if (bytes == null) {
                try {
                    authorRedisRepository.deleteById(key);
                } catch (Exception e) {
                    log.warn("Failed to delete redis key for tombstone: {}", key, e);
                }
                try {
                    String tombstoneJson = jsonMapper.writeValueAsString(
                            Map.of("email", key, "__deleted", true, "__entity", "author"));
                    redis.opsForList().leftPush(queueKey, tombstoneJson);
                } catch (Exception e) {
                    log.error("Failed to enqueue tombstone marker for key: {}, may lose durability", key, e);
                    throw new IllegalStateException("Failed to enqueue tombstone marker for key=" + key, e);
                }
                return;
            }

            JsonNode node = jsonMapper.readTree(bytes);
            if (node.isString() && node.asString().startsWith("eyJ")) {
                log.info("Detected Base64 encoded JSON payload in authors-aggregates, decoding...");
                bytes = java.util.Base64.getDecoder().decode(node.asString());
                node = jsonMapper.readTree(bytes);
            }

            AuthorRequest payload = jsonMapper.treeToValue(node, AuthorRequest.class);

            // Update Redis Repository (Cache)
            try {
                if (deletionMarkerHandler.isDeleted(DeletionMarkerHandler.AUTHOR, key)) {
                    log.info("Skipping Redis update for author {} as it is marked as deleted", key);
                } else {
                    AuthorRedis redisEntity = new AuthorRedis()
                            .setEmail(payload.email())
                            .setFirstName(payload.firstName())
                            .setMiddleName(payload.middleName())
                            .setLastName(payload.lastName())
                            .setMobile(payload.mobile())
                            .setRegisteredAt(payload.registeredAt());
                    redisEntity.setCreatedAt(payload.createdAt());
                    redisEntity.setModifiedAt(payload.modifiedAt());
                    authorRedisRepository.save(redisEntity);
                }
            } catch (Exception e) {
                log.warn("Failed to update author redis repository for key: {}", key, e);
            }

            // Enqueue payload for asynchronous DB writes
            try {
                // Use original node to preserve all fields
                String jsonString = jsonMapper.writeValueAsString(node);
                if (jsonString.startsWith("{")) {
                    // Add entity type for downstream processing
                    jsonString = "{\"__entity\":\"author\"," + jsonString.substring(1);
                }
                redis.opsForList().leftPush(queueKey, jsonString);
            } catch (Exception e) {
                log.error("Failed to enqueue payload for DB write, key: {}, may lose durability", key, e);
            }
        } catch (Exception e) {
            log.error("Unhandled exception in handleAggregate for authors", e);
            throw e;
        }
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, byte[]> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Received dead-letter message from topic {}. Key: {}", topic, record.key());
        // Push failed message to a simple Redis DLQ list for later inspection
        String dlqKey = "dlq:authors-aggregates";
        try {
            String payload = record.value() != null ? new String(record.value()) : "null";
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ: {}", dlqKey, e);
        }
    }
}
