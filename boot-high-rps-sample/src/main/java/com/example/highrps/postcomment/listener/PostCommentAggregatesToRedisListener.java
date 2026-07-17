package com.example.highrps.postcomment.listener;

import com.example.highrps.infrastructure.cache.CacheKeyGenerator;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.postcomment.command.PostCommentCommandResult;
import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.PostCommentRedis;
import com.example.highrps.postcomment.domain.PostCommentRedisRepository;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import com.example.highrps.shared.AbstractAggregatesToRedisListener;
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
public class PostCommentAggregatesToRedisListener extends AbstractAggregatesToRedisListener<PostCommentRequest> {

    private static final Logger log = LoggerFactory.getLogger(PostCommentAggregatesToRedisListener.class);

    private final PostCommentMapper mapper;
    private final PostCommentRedisRepository postCommentRedisRepository;

    public PostCommentAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            PostCommentMapper mapper,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            PostCommentRedisRepository postCommentRedisRepository,
            DeletionMarkerHandler deletionMarkerHandler) {
        super(redis, queueKey, jsonMapper, deletionMarkerHandler, PostCommentRequest.class);
        this.mapper = mapper;
        this.postCommentRedisRepository = postCommentRedisRepository;
    }

    @Override
    protected void deleteFromRepository(String key) {
        postCommentRedisRepository.deleteById(key);
    }

    @Override
    protected void saveToRepository(PostCommentRequest payload, String key) {
        PostCommentRedis redisEntity = mapper.toRedis(payload);
        postCommentRedisRepository.save(redisEntity);
    }

    @Override
    protected String getEntityType() {
        return "post-comment";
    }

    @Override
    protected String getMarkerType() {
        return DeletionMarkerHandler.POST_COMMENT;
    }

    @Override
    protected String getDeletionIdentifierField() {
        return "commentId";
    }

    @Override
    protected boolean isDeletionEvent(JsonNode node) {
        return !node.has("content") && node.has("commentId");
    }

    @Override
    protected void processDeletionEvent(JsonNode node, String key, String topicName) {
        long commentId = node.get("commentId").asLong();
        long postId = node.has("postId") ? node.get("postId").asLong() : 0L;
        String cacheKey = CacheKeyGenerator.generatePostCommentKey(postId, commentId);
        log.info("Processing DELETE event for comment: {}", cacheKey);

        // deleteFromRepository expects commentId
        try {
            deleteFromRepository(String.valueOf(commentId));
        } catch (Exception e) {
            log.warn("Failed to delete Redis entry: {}", commentId, e);
        }

        try {
            deletionMarkerHandler.markDeleted(DeletionMarkerHandler.POST_COMMENT, cacheKey);
            String tombstoneJson = jsonMapper.writeValueAsString(
                    Map.of("id", commentId, "postId", postId, "__deleted", true, "__entity", "post-comment"));
            redis.opsForList().leftPush(queueKey, tombstoneJson);
        } catch (Exception e) {
            log.error("Failed to enqueue delete marker: {}", cacheKey, e);
        }
    }

    @Override
    protected void handleTombstone(String key, String topicName) {
        log.warn("Received null (tombstone) payload in {} for key: {}", topicName, key);
        // Handle tombstone if needed, but usually we handle it via Deleted events or record.key()
        // We do not have postId here, so we cannot form the CacheKey. We rely on processDeletionEvent.
    }

    @Override
    protected String getCacheKey(PostCommentRequest payload, String key) {
        return CacheKeyGenerator.generatePostCommentKey(payload.postId(), payload.commentId());
    }

    @Override
    protected String prepareEnqueuePayload(JsonNode node, PostCommentRequest payload) throws Exception {
        PostCommentCommandResult result = mapper.toResultFromRequest(payload);
        String jsonToEnqueue = mapper.toJson(result);
        if (jsonToEnqueue.startsWith("{")) {
            jsonToEnqueue = "{\"__entity\":\"post-comment\"," + jsonToEnqueue.substring(1);
        }
        return jsonToEnqueue;
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
        processAggregate(record, "post-comments-aggregates");
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, byte[]> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        handleDlt(record, topic);
    }
}
