package com.example.highrps.listener;

import com.example.highrps.author.AuthorRequest;
import com.example.highrps.author.AuthorRequestToResponseMapper;
import com.example.highrps.author.AuthorResponse;
import com.example.highrps.repository.redis.AuthorRedisRepository;
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
import tools.jackson.databind.json.JsonMapper;

@Component
public class AuthorAggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorAggregatesToRedisListener.class);

    private final RedisTemplate<String, String> redis;
    private final AuthorRequestToResponseMapper mapper;
    private final String queueKey;
    private final JsonMapper jsonMapper;
    private final AuthorRedisRepository authorRedisRepository;

    public AuthorAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            AuthorRequestToResponseMapper mapper,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            AuthorRedisRepository authorRedisRepository) {
        this.redis = redis;
        this.mapper = mapper;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
        this.authorRedisRepository = authorRedisRepository;
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
                }
                return;
            }

            tools.jackson.databind.JsonNode node = jsonMapper.readTree(bytes);
            if (node.isTextual() && node.asText().startsWith("eyJ")) {
                log.info("Detected Base64 encoded JSON payload in authors-aggregates, decoding...");
                bytes = java.util.Base64.getDecoder().decode(node.asText());
                node = jsonMapper.readTree(bytes);
            }

            AuthorRequest payload = jsonMapper.treeToValue(node, AuthorRequest.class);

            // Map AuthorRequest to AuthorResponse for Redis storage
            AuthorResponse value = mapper.mapToAuthorResponse(payload);
            var jsonString = AuthorResponse.toJson(value);
            if (jsonString.startsWith("{")) {
                // Add entity type for downstream processing
                jsonString = "{\"__entity\":\"author\"," + jsonString.substring(1);
            }

            // Enqueue the same payload for asynchronous DB writes
            try {
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
