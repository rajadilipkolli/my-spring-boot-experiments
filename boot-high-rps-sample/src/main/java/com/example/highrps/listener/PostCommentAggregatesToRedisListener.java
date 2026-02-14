package com.example.highrps.listener;

import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import com.example.highrps.postcomment.domain.PostCommentResult;
import com.example.highrps.repository.redis.PostCommentRedisRepository;
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
public class PostCommentAggregatesToRedisListener {

    private static final Logger log = LoggerFactory.getLogger(PostCommentAggregatesToRedisListener.class);

    private final RedisTemplate<String, String> redis;
    private final PostCommentMapper mapper;
    private final String queueKey;
    private final JsonMapper jsonMapper;
    private final PostCommentRedisRepository postCommentRedisRepository;

    public PostCommentAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            PostCommentMapper mapper,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            PostCommentRedisRepository postCommentRedisRepository) {
        this.redis = redis;
        this.mapper = mapper;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
        this.postCommentRedisRepository = postCommentRedisRepository;
    }

    @KafkaListener(
            topics = "post-comments-aggregates",
            groupId = "aggregates-redis-writer",
            containerFactory = "postCommentKafkaListenerContainerFactory")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 500, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    public void handleAggregate(ConsumerRecord<String, PostCommentRequest> record) {
        // Log record metadata early to diagnose tombstone timing issues
        try {
            String cacheKey = record.key();
            PostCommentRequest payload = record.value();
            log.debug(
                    "Received post-comments-aggregates record: partition={}, offset={}, cacheKey={}, valueIsNull={}",
                    record.partition(),
                    record.offset(),
                    cacheKey,
                    payload == null);

            // If payload is null -> tombstone: remove Redis cacheKey and enqueue delete
            // marker
            if (payload == null) {
                try {
                    // remove repository-backed entry
                    postCommentRedisRepository.deleteById(cacheKey);
                } catch (Exception e) {
                    log.warn("Failed to delete repository entry for tombstone cacheKey: {}", cacheKey, e);
                }
                try {
                    String tombstoneJson = jsonMapper.writeValueAsString(
                            Map.of("id", cacheKey, "__deleted", true, "__entity", "post-comment"));
                    redis.opsForList().leftPush(queueKey, tombstoneJson);
                } catch (Exception e) {
                    log.error("Failed to enqueue tombstone marker for cacheKey: {}, may lose durability", cacheKey, e);
                }
                return;
            }

            // Map PostCommentRequest to PostCommentResult for Redis storage
            PostCommentResult value = mapper.toResultFromRequest(payload);
            var jsonString = mapper.toJson(value);
            if (jsonString.startsWith("{")) {
                // Add entity type for downstream processing
                jsonString = "{\"__entity\":\"post-comment\"," + jsonString.substring(1);
            }

            // Update Redis repository
            try {
                postCommentRedisRepository.save(mapper.toRedis(payload));
            } catch (Exception e) {
                log.warn("Failed to update Redis repository for cacheKey: {}", cacheKey, e);
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
    public void dlt(
            ConsumerRecord<String, PostCommentRequest> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Received dead-letter message : {} from topic {}", record.value(), topic);
        // Push failed message to a simple Redis DLQ list for later inspection
        String dlqKey = "dlq:post-comments-aggregates";
        try {
            PostCommentRequest commentRequest = record.value();
            if (commentRequest == null) {
                log.warn("DLT record has null value; key={}", record.key());
                return;
            }
            String payload = mapper.toJson(mapper.toResultFromRequest(commentRequest));
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ: {}", dlqKey, e);
        }
    }
}
