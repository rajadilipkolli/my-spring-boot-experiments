package com.example.highrps.listener;

import com.example.highrps.post.domain.PostResponse;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.post.mapper.PostRequestToResponseMapper;
import com.example.highrps.repository.redis.PostRedisRepository;
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
public class PostAggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(PostAggregatesToRedisListener.class);

    private final RedisTemplate<String, String> redis;
    private final PostRequestToResponseMapper mapper;
    private final String queueKey;
    private final JsonMapper jsonMapper;
    private final PostRedisRepository postRedisRepository;

    public PostAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            PostRequestToResponseMapper mapper,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            PostRedisRepository postRedisRepository) {
        this.redis = redis;
        this.mapper = mapper;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
        this.postRedisRepository = postRedisRepository;
    }

    @KafkaListener(
            topics = "posts-aggregates",
            groupId = "new-posts-redis-writer",
            containerFactory = "newPostKafkaListenerContainerFactory")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 500, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    public void handleAggregate(ConsumerRecord<String, byte[]> record) {
        // Log record metadata early to diagnose tombstone timing issues
        try {
            String cacheKey = record.key();
            byte[] bytes = record.value();
            log.debug(
                    "Received posts-aggregates record: partition={}, offset={}, cacheKey={}, valueIsNull={}",
                    record.partition(),
                    record.offset(),
                    cacheKey,
                    bytes == null);
            // If payload is null -> tombstone: remove Redis cacheKey and enqueue delete marker
            if (bytes == null) {
                handleDeletion(cacheKey);
                return;
            }

            tools.jackson.databind.JsonNode node = jsonMapper.readTree(bytes);
            if (node.isTextual() && node.asText().startsWith("eyJ")) {
                log.info("Detected Base64 encoded JSON payload in posts-aggregates, decoding...");
                bytes = java.util.Base64.getDecoder().decode(node.asText());
                node = jsonMapper.readTree(bytes);
            }

            // Check for explicit deletion event (PostDeletedEvent has only postId)
            if (node.has("postId") && node.size() == 1) {
                log.info("Identified deletion event (PostDeletedEvent) for cacheKey: {}", cacheKey);
                handleDeletion(cacheKey);
                return;
            }

            NewPostRequest payload = jsonMapper.treeToValue(node, NewPostRequest.class);

            // Map NewPostRequest to PostResponse for Redis storage
            PostResponse value = mapper.mapToPostResponse(payload);
            String jsonString = PostResponse.toJson(value);
            if (jsonString.startsWith("{")) {
                // Add entity type for downstream processing
                jsonString = "{\"__entity\":\"post\"," + jsonString.substring(1);
            }

            // Enqueue the same payload for asynchronous DB writes
            try {
                redis.opsForList().leftPush(queueKey, jsonString);
            } catch (Exception e) {
                log.error("Failed to enqueue payload for DB write, cacheKey: {}, may lose durability", cacheKey, e);
            }
        } catch (Exception e) {
            log.error("Unhandled exception in handleAggregate", e);
            throw e;
        }
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, byte[]> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Received dead-letter message from topic {}", topic);
        // Push failed message to a simple Redis DLQ list for later inspection
        String dlqKey = "dlq:posts-aggregates";
        try {
            String payload = record.value() != null ? new String(record.value()) : "null";
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ: {}", dlqKey, e);
        }
    }

    private void handleDeletion(String cacheKey) {
        try {
            // remove repository-backed entry
            postRedisRepository.deleteById(Long.valueOf(cacheKey));
        } catch (Exception e) {
            log.warn("Failed to delete repository entry for deletion of cacheKey: {}", cacheKey, e);
        }
        try {
            String tombstoneJson =
                    jsonMapper.writeValueAsString(Map.of("postId", cacheKey, "__deleted", true, "__entity", "post"));
            redis.opsForList().leftPush(queueKey, tombstoneJson);
        } catch (Exception e) {
            log.error("Failed to enqueue tombstone marker for cacheKey: {}, may lose durability", cacheKey, e);
        }
    }
}
