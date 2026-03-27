package com.example.highrps.postcomment.command;

import com.example.highrps.infrastructure.cache.CacheKeyGenerator;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.query.PostQueryService;
import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import com.example.highrps.postcomment.domain.events.PostCommentCreatedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentDeletedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentUpdatedEvent;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.postcomment.query.GetPostCommentQuery;
import com.example.highrps.postcomment.query.PostCommentQueryService;
import com.example.highrps.repository.redis.PostCommentRedisRepository;
import com.example.highrps.shared.IdGenerator;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Command service for PostComment aggregate.
 * Handles all write operations and publishes domain events.
 */
@Service
@Transactional
public class PostCommentCommandService {

    private static final Logger log = LoggerFactory.getLogger(PostCommentCommandService.class);

    private final PostQueryService postQueryService;
    private final PostCommentQueryService postCommentQueryService;
    private final ApplicationEventPublisher events;
    private final Cache<String, String> localCache;
    private final PostCommentRedisRepository postCommentRedisRepository;
    private final PostCommentMapper postCommentMapper;
    private final Counter eventsPublishedCounter;
    private final Counter tombstonesPublishedCounter;
    private final DeletionMarkerHandler deletionMarkerHandler;

    public PostCommentCommandService(
            PostQueryService postQueryService,
            PostCommentQueryService postCommentQueryService,
            ApplicationEventPublisher events,
            Cache<String, String> localCache,
            PostCommentRedisRepository postCommentRedisRepository,
            PostCommentMapper postCommentMapper,
            MeterRegistry meterRegistry,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.postQueryService = postQueryService;
        this.postCommentQueryService = postCommentQueryService;
        this.events = events;
        this.localCache = localCache;
        this.postCommentRedisRepository = postCommentRedisRepository;
        this.postCommentMapper = postCommentMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;

        this.eventsPublishedCounter = Counter.builder("post-comments.events.published")
                .description("Number of post comment events published")
                .register(meterRegistry);
        this.tombstonesPublishedCounter = Counter.builder("post-comments.tombstones.published")
                .description("Number of post comment tombstone events published")
                .register(meterRegistry);
    }

    /**
     * Create a new comment with event-driven pattern using application events.
     */
    public PostCommentCommandResult createComment(CreatePostCommentCommand cmd) {
        // Validate post exists
        if (!postQueryService.exists(cmd.postId())) {
            throw new ResourceNotFoundException("Post not found with id: " + cmd.postId());
        }

        Long commentId = IdGenerator.generateLong();

        // Build result and populate event
        PostCommentRequest request = PostCommentRequest.fromCreateCmd(cmd, commentId);
        PostCommentCreatedEvent event = new PostCommentCreatedEvent(
                commentId,
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                cmd.published(),
                request.publishedAt(),
                request.createdAt().atOffset(ZoneOffset.UTC));
        events.publishEvent(event);

        // Increment counter
        eventsPublishedCounter.increment();

        // Build result
        PostCommentCommandResult result = postCommentMapper.toResultFromRequest(request);

        // Eager cache updates
        executeAfterCommit(() -> updateCaches(cmd.postId(), commentId, result));

        log.info("Created post comment: postId={}, commentId={}", cmd.postId(), commentId);
        return result;
    }

    /**
     * Update a comment with event-driven pattern.
     */
    public void updateComment(UpdatePostCommentCommand cmd) {
        // Validate comment exists
        PostCommentCommandResult existing =
                postCommentQueryService.getCommentById(new GetPostCommentQuery(cmd.postId(), cmd.commentId()));

        // Build request and populate event
        PostCommentRequest request = PostCommentRequest.fromUpdateCmd(cmd, existing.createdAt());
        PostCommentUpdatedEvent event = new PostCommentUpdatedEvent(
                cmd.commentId().id(),
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                cmd.published(),
                request.publishedAt(),
                request.createdAt(),
                request.modifiedAt());
        events.publishEvent(event);

        // Increment counter
        eventsPublishedCounter.increment();

        // Build result
        PostCommentCommandResult result = postCommentMapper.toResultFromRequest(request);

        // Eager cache updates
        executeAfterCommit(() -> updateCaches(cmd.postId(), cmd.commentId().id(), result));

        log.info(
                "Updated post comment: postId={}, commentId={}",
                cmd.postId(),
                cmd.commentId().id());
    }

    /**
     * Delete a comment with event-driven pattern.
     */
    public void deleteComment(PostCommentId commentId, Long postId) {
        var cacheKey = CacheKeyGenerator.generatePostCommentKey(postId, commentId.id());

        // 1. Publish tombstone event
        events.publishEvent(new PostCommentDeletedEvent(commentId.id(), postId));
        tombstonesPublishedCounter.increment();

        executeAfterCommit(() -> {
            // 2. Invalidate local cache
            try {
                localCache.invalidate(cacheKey);
            } catch (Exception e) {
                log.warn("Failed to invalidate local cache for comment: {}", commentId.id(), e);
            }

            // 3. Remove from Redis
            try {
                postCommentRedisRepository.deleteById(cacheKey);
            } catch (Exception e) {
                log.warn("Failed to delete Redis entry for comment: {}", commentId.id(), e);
            }

            // 4. Mark deleted in Redis with TTL using unified handler
            deletionMarkerHandler.markDeleted(DeletionMarkerHandler.POST_COMMENT, cacheKey);
        });

        log.info("Deleted post comment: postId={}, commentId={}", postId, commentId.id());
    }

    private void executeAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private void updateCaches(Long postId, Long commentId, PostCommentCommandResult result) {
        String cacheKey = CacheKeyGenerator.generatePostCommentKey(postId, commentId);

        // Update local cache
        try {
            String json = postCommentMapper.toJson(result);
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for comment: {}", commentId, e);
        }

        // Update Redis
        try {
            PostCommentRequest request = postCommentMapper.toRequestFromResult(result);
            postCommentRedisRepository.save(postCommentMapper.toRedis(request));
        } catch (Exception e) {
            log.warn("Failed to update Redis for comment: {}", commentId, e);
        }
    }
}
