package com.example.highrps.post.command;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.PostRedis;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.post.domain.events.PostCreatedEvent;
import com.example.highrps.post.domain.events.PostDeletedEvent;
import com.example.highrps.post.domain.events.PostUpdatedEvent;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Command service for Post aggregate.
 * Handles all write operations (create, update, delete) and publishes domain
 * events.
 */
@Service
public class PostCommandService {

    private static final Logger log = LoggerFactory.getLogger(PostCommandService.class);
    private static final Executor VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Cache<String, String> localCache;
    private final PostRedisRepository postRedisRepository;
    private final JsonMapper jsonMapper;
    private final DeletionMarkerHandler deletionMarkerHandler;
    private final ConcurrentHashMap<String, CompletableFuture<Void>> cacheMutationFutures = new ConcurrentHashMap<>();

    public PostCommandService(
            KafkaTemplate<String, Object> kafkaTemplate,
            Cache<String, String> localCache,
            PostRedisRepository postRedisRepository,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.kafkaTemplate = kafkaTemplate;
        this.localCache = localCache;
        this.postRedisRepository = postRedisRepository;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
    }

    public CompletableFuture<PostCommandResult> createPost(CreatePostCommand cmd) {
        log.info("Creating post with id: {}", cmd.postId());

        // Generate timestamps
        LocalDateTime now = LocalDateTime.now();

        boolean isPublished = Boolean.TRUE.equals(cmd.published());
        LocalDateTime publishedAt = isPublished ? now : null;

        // Map to ensure correct timestamps for the new state
        PostDetailsResponse detailsResponse = cmd.details() != null
                ? new PostDetailsResponse(
                        cmd.details().detailsKey(), now, cmd.details().createdBy())
                : null;

        List<TagResponse> tags = cmd.tags() != null
                ? cmd.tags().stream()
                        .map(t -> new TagResponse(null, t.tagName(), t.tagDescription()))
                        .toList()
                : List.of();

        // Publish domain event (transactional - committed with transaction)
        PostCreatedEvent event = new PostCreatedEvent(
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                cmd.authorEmail(),
                isPublished,
                publishedAt,
                now,
                detailsResponse,
                tags);
        // Send directly to Kafka
        PostCommandResult result = new PostCommandResult(
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                cmd.authorEmail(),
                isPublished,
                publishedAt,
                now,
                null, // modifiedAt should be null on creation
                detailsResponse,
                tags);

        return kafkaTemplate
                .send("posts-aggregates", String.valueOf(cmd.postId()), event)
                .handleAsync(
                        (res, err) -> {
                            if (err != null) {
                                log.error("Failed to publish create post event for postId: {}", cmd.postId(), err);
                                throw new RuntimeException("Failed to publish create post event", err);
                            } else {
                                log.info("Successfully published create post event for postId: {}", cmd.postId());
                                return result;
                            }
                        },
                        VIRTUAL_EXECUTOR)
                .thenComposeAsync(
                        postCommandResult -> enqueueAggregateOperation(String.valueOf(cmd.postId()), () -> {
                                    updateCaches(cmd.postId(), postCommandResult);
                                    log.info("Post created successfully: {}", cmd.postId());
                                    return CompletableFuture.completedFuture(null);
                                })
                                .thenApply(ignored -> postCommandResult),
                        VIRTUAL_EXECUTOR);
    }

    public CompletableFuture<PostCommandResult> updatePost(UpdatePostCommand cmd) {
        log.info("Updating post with id: {}", cmd.postId());

        // Retrieve existing post info for createdAt
        LocalDateTime createdAt = getCreatedAt(cmd.postId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishedAt = (cmd.published() != null && cmd.published()) ? now : null;

        // Retrieve existing author email
        String authorEmail = getAuthorEmail(cmd.postId());

        // Map to ensure correct timestamps
        PostDetailsResponse detailsResponse = cmd.details() != null
                ? new PostDetailsResponse(
                        cmd.details().detailsKey(), now, cmd.details().createdBy())
                : null;

        List<TagResponse> tags = cmd.tags() != null
                ? cmd.tags().stream()
                        .map(t -> new TagResponse(null, t.tagName(), t.tagDescription()))
                        .toList()
                : null;

        // Publish domain event
        PostUpdatedEvent event = new PostUpdatedEvent(
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                authorEmail,
                cmd.published() != null && cmd.published(),
                publishedAt,
                createdAt,
                now,
                detailsResponse,
                tags);
        // Send directly to Kafka

        return kafkaTemplate
                .send("posts-aggregates", String.valueOf(cmd.postId()), event)
                .handleAsync(
                        (res, err) -> {
                            if (err != null) {
                                log.error("Failed to publish update post event for postId: {}", cmd.postId(), err);
                                throw new RuntimeException("Failed to publish update post event", err);
                            } else {
                                log.info("Successfully published update post event for postId: {}", cmd.postId());
                                return new PostCommandResult(
                                        cmd.postId(),
                                        cmd.title(),
                                        cmd.content(),
                                        authorEmail,
                                        cmd.published() != null && cmd.published(),
                                        publishedAt,
                                        createdAt,
                                        now,
                                        detailsResponse,
                                        tags);
                            }
                        },
                        VIRTUAL_EXECUTOR)
                .thenComposeAsync(
                        postCommandResult -> enqueueAggregateOperation(String.valueOf(cmd.postId()), () -> {
                                    updateCaches(cmd.postId(), postCommandResult);
                                    log.info("Post updated successfully: {}", cmd.postId());
                                    return CompletableFuture.completedFuture(null);
                                })
                                .thenApply(ignored -> postCommandResult),
                        VIRTUAL_EXECUTOR);
    }

    public CompletableFuture<Void> deletePost(Long postId) {
        log.info("Deleting post with id: {}", postId);

        // 1. Publish tombstone event
        // 1. Send directly to Kafka
        return kafkaTemplate
                .send("posts-aggregates", String.valueOf(postId), new PostDeletedEvent(postId))
                .handleAsync(
                        (res, err) -> {
                            if (err != null) {
                                log.error("Failed to publish delete post event for postId: {}", postId, err);
                                throw new RuntimeException("Failed to publish delete post event", err);
                            } else {
                                log.info("Successfully published delete post event for postId: {}", postId);
                                return null;
                            }
                        },
                        VIRTUAL_EXECUTOR)
                .thenComposeAsync(
                        ignored -> enqueueAggregateOperation(String.valueOf(postId), () -> {
                            // 2. Invalidate local cache
                            String cacheKey = String.valueOf(postId);
                            localCache.invalidate(cacheKey);

                            // 3. Mark deleted in Redis with TTL (prevents batch re-insertion)
                            deletionMarkerHandler.markDeleted(DeletionMarkerHandler.POST, String.valueOf(postId));

                            // Note: We intentionally do NOT delete from postRedisRepository here.
                            // The background AggregatesToRedisListener will process the tombstone
                            // and delete the record asynchronously.

                            log.info("Post deleted successfully: {}", postId);
                            return CompletableFuture.completedFuture(null);
                        }),
                        VIRTUAL_EXECUTOR);
    }

    private CompletableFuture<Void> enqueueAggregateOperation(
            String aggregateKey, Supplier<CompletableFuture<Void>> operation) {
        CompletableFuture<Void> previous =
                cacheMutationFutures.computeIfAbsent(aggregateKey, key -> CompletableFuture.completedFuture(null));
        CompletableFuture<Void> current = previous.thenComposeAsync(ignored -> operation.get(), VIRTUAL_EXECUTOR);
        cacheMutationFutures.put(aggregateKey, current);
        current.whenComplete((ignored, throwable) -> {
            if (cacheMutationFutures.get(aggregateKey) == current) {
                cacheMutationFutures.remove(aggregateKey, current);
            }
        });
        return current;
    }

    private void updateCaches(Long postId, PostCommandResult result) {
        String cacheKey = String.valueOf(postId);

        // Update local cache
        try {
            String json = jsonMapper.writeValueAsString(result);
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for postId: {}", postId, e);
        }

        // Note: We intentionally do NOT update postRedisRepository here.
        // The background AggregatesToRedisListener will process the event and update Redis asynchronously.
    }

    private LocalDateTime getCreatedAt(Long postId) {
        // Try to get from Redis first
        try {
            return postRedisRepository
                    .findById(postId)
                    .map(PostRedis::getCreatedAt)
                    .orElse(LocalDateTime.now());
        } catch (Exception e) {
            log.warn("Failed to get createdAt for postId: {}, using now()", postId, e);
            return LocalDateTime.now();
        }
    }

    private String getAuthorEmail(Long postId) {
        try {
            return postRedisRepository
                    .findById(postId)
                    .map(PostRedis::getAuthorEmail)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to get authorEmail for postId: {}", postId, e);
            return null;
        }
    }
}
