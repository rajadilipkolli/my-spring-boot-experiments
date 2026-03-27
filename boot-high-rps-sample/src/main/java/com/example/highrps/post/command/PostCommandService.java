package com.example.highrps.post.command;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.PostRedis;
import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.post.domain.events.PostCreatedEvent;
import com.example.highrps.post.domain.events.PostDeletedEvent;
import com.example.highrps.post.domain.events.PostUpdatedEvent;
import com.example.highrps.repository.redis.PostRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

/**
 * Command service for Post aggregate.
 * Handles all write operations (create, update, delete) and publishes domain
 * events.
 */
@Service
@Transactional
public class PostCommandService {

    private static final Logger log = LoggerFactory.getLogger(PostCommandService.class);

    private final ApplicationEventPublisher events;
    private final Cache<String, String> localCache;
    private final PostRedisRepository postRedisRepository;
    private final JsonMapper objectMapper;
    private final DeletionMarkerHandler deletionMarkerHandler;

    public PostCommandService(
            ApplicationEventPublisher events,
            Cache<String, String> localCache,
            PostRedisRepository postRedisRepository,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.events = events;
        this.localCache = localCache;
        this.postRedisRepository = postRedisRepository;
        this.objectMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
    }

    public PostCommandResult createPost(CreatePostCommand cmd) {
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
        events.publishEvent(event);

        // Eager cache update (best effort)
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

        // Eager cache update
        executeAfterCommit(() -> updateCaches(cmd.postId(), result));

        log.info("Post created successfully: {}", cmd.postId());
        return result;
    }

    public PostCommandResult updatePost(UpdatePostCommand cmd) {
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
                : List.of();

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
        events.publishEvent(event);

        // Eager cache update
        PostCommandResult result = new PostCommandResult(
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

        // Eager cache update
        executeAfterCommit(() -> updateCaches(cmd.postId(), result));

        log.info("Post updated successfully: {}", cmd.postId());
        return result;
    }

    public void deletePost(Long postId) {
        log.info("Deleting post with id: {}", postId);

        // 1. Publish tombstone event
        events.publishEvent(new PostDeletedEvent(postId));

        executeAfterCommit(() -> {
            // 2. Invalidate local cache
            String cacheKey = String.valueOf(postId);
            localCache.invalidate(cacheKey);

            // 3. Remove from Redis
            try {
                postRedisRepository.deleteById(postId);
            } catch (Exception e) {
                log.warn("Failed to delete Redis entry for postId: {}", postId, e);
            }

            // 4. Mark deleted in Redis with TTL (prevents batch re-insertion)
            deletionMarkerHandler.markDeleted(DeletionMarkerHandler.POST, String.valueOf(postId));
        });

        log.info("Post deleted successfully: {}", postId);
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

    private void updateCaches(Long postId, PostCommandResult result) {
        String cacheKey = String.valueOf(postId);

        // Update local cache
        try {
            String json = objectMapper.writeValueAsString(result);
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for postId: {}", postId, e);
        }

        // Update Redis
        try {
            PostRedis postRedis = new PostRedis()
                    .setId(result.postId())
                    .setTitle(result.title())
                    .setContent(result.content())
                    .setPublished(result.published())
                    .setPublishedAt(result.publishedAt())
                    .setAuthorEmail(result.authorEmail())
                    .setDetails(result.details())
                    .setTags(result.tags());
            postRedis.setCreatedAt(result.createdAt());
            postRedis.setModifiedAt(result.modifiedAt());

            postRedisRepository.save(postRedis);
        } catch (Exception e) {
            log.warn("Failed to update Redis for postId: {}", postId, e);
        }
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
