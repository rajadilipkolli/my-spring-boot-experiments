package com.example.highrps.postcomment.domain;

import com.example.highrps.config.AppProperties;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.repository.redis.PostCommentRedisRepository;
import com.example.highrps.service.KafkaProducerService;
import com.example.highrps.service.PostService;
import com.example.highrps.shared.IdGenerator;
import com.example.highrps.shared.ResourceNotFoundException;
import com.example.highrps.utility.CacheKeyGenerator;
import com.example.highrps.utility.KafkaPublishHelper;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostCommentCommandService {

    private static final Logger log = LoggerFactory.getLogger(PostCommentCommandService.class);

    private final PostService postService;
    private final PostCommentQueryService postCommentQueryService;
    private final KafkaProducerService kafkaProducerService;
    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final PostCommentRedisRepository postCommentRedisRepository;
    private final PostCommentMapper postCommentMapper;
    private final AppProperties appProperties;
    private final Counter eventsPublishedCounter;
    private final Counter tombstonesPublishedCounter;

    public PostCommentCommandService(
            PostService postService,
            PostCommentQueryService postCommentQueryService,
            KafkaProducerService kafkaProducerService,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            PostCommentRedisRepository postCommentRedisRepository,
            PostCommentMapper postCommentMapper,
            AppProperties appProperties,
            MeterRegistry meterRegistry) {
        this.postService = postService;
        this.postCommentQueryService = postCommentQueryService;
        this.kafkaProducerService = kafkaProducerService;
        this.localCache = localCache;
        this.redis = redis;
        this.postCommentRedisRepository = postCommentRedisRepository;
        this.postCommentMapper = postCommentMapper;
        this.appProperties = appProperties;

        this.eventsPublishedCounter = Counter.builder("post-comments.events.published")
                .description("Number of post comment events published to Kafka")
                .register(meterRegistry);
        this.tombstonesPublishedCounter = Counter.builder("post-comments.tombstones.published")
                .description("Number of post comment tombstone events published to Kafka")
                .register(meterRegistry);
    }

    /**
     * Create a new comment with event-driven pattern:
     * 1. Validate post exists
     * 2. Generate ID and Publish to Kafka
     * 3. Update local cache
     * 4. Eager write to Redis
     */
    public PostCommentResult createComment(CreatePostCommentCmd cmd) {
        // Validate post exists before generating ID and publishing to Kafka
        if (!postService.existsByPostRefId(cmd.postId())) {
            throw new ResourceNotFoundException("Post not found with id: " + cmd.postId());
        }

        Long commentId = IdGenerator.generateLong();

        // Build request for Kafka event
        PostCommentRequest request = PostCommentRequest.fromCreateCmd(cmd, commentId);
        var cacheKey = CacheKeyGenerator.generatePostCommentKey(cmd.postId(), commentId);

        // Publish to Kafka and wait for acknowledgment
        var future = kafkaProducerService.publishEnvelope("post-comment", cacheKey, request);
        KafkaPublishHelper.awaitPublish(future, "post-comment", cacheKey, appProperties.getPublishTimeOutMs());

        // Increment events counter after successful publish
        eventsPublishedCounter.increment();

        // Build the response for caching
        PostCommentResult result = postCommentMapper.toResultFromRequest(request);
        String json = postCommentMapper.toJson(result);

        // Update local cache (best-effort)
        try {
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for comment {} after successful publish", commentId, e);
        }

        // Eager write to Redis (best-effort)
        try {
            postCommentRedisRepository.save(postCommentMapper.toRedis(request));
        } catch (Exception e) {
            log.warn(
                    "Eager redis repository write failed for comment {} (listener will still populate redis)",
                    commentId,
                    e);
        }

        return result;
    }

    /**
     * Update a comment with event-driven pattern:
     * 1. Validate post and comment exists
     * 2. Publish to Kafka
     * 3. Update local cache
     * 4. Eager write to Redis
     */
    public void updateComment(UpdatePostCommentCmd cmd) {

        // Validate post and comment exists before publishing to Kafka
        PostCommentResult commentById =
                postCommentQueryService.getCommentById(new GetPostCommentQuery(cmd.postId(), cmd.commentId()));
        if (commentById == null) {
            throw new ResourceNotFoundException("Post not found with id: " + cmd.postId() + " and comment id: "
                    + cmd.commentId().id());
        }
        // Build request for Kafka event
        PostCommentRequest request = PostCommentRequest.fromUpdateCmd(cmd, commentById.createdAt());
        var cacheKey = CacheKeyGenerator.generatePostCommentKey(
                cmd.postId(), cmd.commentId().id());

        // Publish to Kafka and wait for acknowledgment
        var future = kafkaProducerService.publishEnvelope("post-comment", cacheKey, request);
        KafkaPublishHelper.awaitPublish(future, "post-comment", cacheKey, appProperties.getPublishTimeOutMs());

        // Increment events counter after successful publish
        eventsPublishedCounter.increment();

        // Build the response for caching
        PostCommentResult result = postCommentMapper.toResultFromRequest(request);
        String json = postCommentMapper.toJson(result);

        // Update local cache (best-effort)
        try {
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn(
                    "Failed to update local cache for comment {} after successful publish",
                    cmd.commentId().id(),
                    e);
        }

        // Eager write to Redis (best-effort)
        try {
            postCommentRedisRepository.save(postCommentMapper.toRedis(request));
        } catch (Exception e) {
            log.warn(
                    "Eager redis repository write failed for comment {} (listener will still populate redis)",
                    cmd.commentId().id(),
                    e);
        }
    }

    /**
     * Delete a comment with event-driven pattern:
     * 1. Publish tombstone to Kafka
     * 2. Invalidate local cache
     * 3. Delete from Redis repository
     * 4. Mark deleted in Redis with TTL
     */
    public void deleteComment(PostCommentId commentId, Long postId) {
        var cacheKey = CacheKeyGenerator.generatePostCommentKey(postId, commentId.id());

        // 1) Publish tombstone to Kafka
        try {
            var deleteForEntity = kafkaProducerService.publishDeleteForEntity("post-comment", cacheKey);
            KafkaPublishHelper.awaitPublish(
                    deleteForEntity, "post-comment", cacheKey, appProperties.getPublishTimeOutMs());
            log.info("deleteComment: published tombstone for postId={} commentId={}", postId, commentId.id());
            // Increment tombstone counter after successful publish
            tombstonesPublishedCounter.increment();
        } catch (Exception e) {
            log.warn("Failed to publish delete event for comment: {}", commentId.id(), e);
            throw new IllegalStateException("Failed to publish delete event for comment: " + commentId.id());
        }

        // 2) Invalidate local cache
        try {
            localCache.invalidate(cacheKey);
            log.info("deleteComment: invalidated local cache for postId={} commentId={}", postId, commentId.id());
        } catch (Exception e) {
            log.warn("Failed to invalidate local cache for comment: {}", commentId.id(), e);
        }

        // 3) Remove from Redis repository
        try {
            postCommentRedisRepository.deleteById(cacheKey);
            log.info(
                    "deleteComment: deleted redis repository entries for postId={} commentId={}",
                    postId,
                    commentId.id());
        } catch (Exception e) {
            log.warn("Failed to delete redis repository entry for comment: {}", commentId.id(), e);
        }

        // 4) Mark deleted in Redis with TTL
        try {
            redis.opsForValue().set("deleted:post-comments:" + cacheKey, "1", Duration.ofSeconds(60));
        } catch (Exception ex) {
            log.warn("Failed to mark comment {} in deleted:post-comments set", commentId.id(), ex);
        }
    }
}
