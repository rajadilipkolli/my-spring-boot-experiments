package com.example.highrps.postcomment.listener;

import com.example.highrps.infrastructure.cache.CacheKeyGenerator;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.postcomment.command.PostCommentCommandResult;
import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.PostCommentRedis;
import com.example.highrps.postcomment.domain.PostCommentRedisRepository;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import java.util.Base64;
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
public class PostCommentAggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(PostCommentAggregatesToRedisListener.class);

    private final RedisTemplate<String, String> redis;
    private final PostCommentMapper mapper;
    private final String queueKey;
    private final JsonMapper jsonMapper;
    private final PostCommentRedisRepository postCommentRedisRepository;
    private final DeletionMarkerHandler deletionMarkerHandler;

    public PostCommentAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            PostCommentMapper mapper,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            PostCommentRedisRepository postCommentRedisRepository,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.redis = redis;
        this.mapper = mapper;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
        this.postCommentRedisRepository = postCommentRedisRepository;
        this.deletionMarkerHandler = deletionMarkerHandler;
    }

    @KafkaListener(
            topics = "post-comments-aggregates",
            groupId = "post-comments-redis-writer",
            containerFactory = "postCommentKafkaListenerContainerFactory")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 500, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    public void handleAggregate(ConsumerRecord<String, byte[]> record) {
        try {
            byte[] bytes = record.value();
            if (bytes == null) {
                log.warn("Received null (tombstone) payload in post-comments-aggregates for key: {}", record.key());
                // Handle tombstone if needed, but usually we handle it via Deleted events or record.key()
                return;
            }

            JsonNode node = jsonMapper.readTree(bytes);

            // Resilience: Detect and decode Base64 encoded JSON (Spring Modulith often does this when externalizing)
            if (node.isString()) {
                String text = node.asString();
                if (text.startsWith("eyJ")) {
                    log.info("Detected Base64 encoded JSON payload in post-comments-aggregates, decoding...");
                    try {
                        bytes = Base64.getDecoder().decode(text);
                        node = jsonMapper.readTree(bytes);
                    } catch (Exception e) {
                        log.warn("Failed to decode base64 node: {}", text.substring(0, Math.min(text.length(), 20)), e);
                    }
                }
            }

            // Check for Delete event (no content field)
            if (!node.has("content") && node.has("commentId")) {
                long commentId = node.get("commentId").asLong();
                long postId = node.get("postId").asLong();
                String cacheKey = CacheKeyGenerator.generatePostCommentKey(postId, commentId);
                log.info("Processing DELETE event for comment: {}", cacheKey);

                try {
                    postCommentRedisRepository.deleteById(cacheKey);
                } catch (Exception e) {
                    log.warn("Failed to delete Redis entry: {}", cacheKey, e);
                }

                try {
                    deletionMarkerHandler.markDeleted(DeletionMarkerHandler.POST_COMMENT, cacheKey);
                    String tombstoneJson = jsonMapper.writeValueAsString(
                            Map.of("id", commentId, "postId", postId, "__deleted", true, "__entity", "post-comment"));
                    redis.opsForList().leftPush(queueKey, tombstoneJson);
                } catch (Exception e) {
                    log.error("Failed to enqueue delete marker: {}", cacheKey, e);
                }
                return;
            }

            // Created or Updated event
            PostCommentRequest request = jsonMapper.treeToValue(node, PostCommentRequest.class);
            if (request == null || request.commentId() == null) {
                log.error("Failed to parse PostCommentRequest from node: {}", node);
                return;
            }

            PostCommentCommandResult result = mapper.toResultFromRequest(request);
            String jsonToEnqueue = mapper.toJson(result);
            if (jsonToEnqueue.startsWith("{")) {
                jsonToEnqueue = "{\"__entity\":\"post-comment\"," + jsonToEnqueue.substring(1);
            }

            // 1. Update Redis repository
            try {
                String key = CacheKeyGenerator.generatePostCommentKey(request.postId(), request.commentId());
                if (deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST_COMMENT, key)) {
                    log.info("Skipping Redis update for postComment {} as it is marked as deleted", key);
                } else {
                    PostCommentRedis redisEntity = mapper.toRedis(request);
                    postCommentRedisRepository.save(redisEntity);
                }
            } catch (Exception e) {
                log.warn("Failed to sync comment to Redis: {}", request.commentId(), e);
            }

            // 2. Enqueue for JPA Batch Persistence
            try {
                log.info("Enqueuing post-comment for DB write: id={}", request.commentId());
                redis.opsForList().leftPush(queueKey, jsonToEnqueue);
            } catch (Exception e) {
                log.error("Failed to enqueue comment for DB write: {}", request.commentId(), e);
                throw e;
            }

        } catch (Exception e) {
            log.error("Failed to process post-comment aggregate event", e);
            throw new RuntimeException("Event processing failed", e);
        }
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, byte[]> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Message sent to DLT from topic {}. Key: {}", topic, record.key());
        try {
            String dlqKey = "dlq:post-comments-aggregates";
            String payload = record.value() != null ? new String(record.value()) : "null";
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ", e);
        }
    }
}
