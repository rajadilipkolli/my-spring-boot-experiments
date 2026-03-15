package com.example.highrps.post.command;

import com.example.highrps.post.PostRedis;
import com.example.highrps.post.domain.events.PostCreatedEvent;
import com.example.highrps.post.domain.events.PostDeletedEvent;
import com.example.highrps.post.domain.events.PostUpdatedEvent;
import com.example.highrps.repository.redis.PostRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final RedisTemplate<String, String> redis;
    private final PostRedisRepository postRedisRepository;
    private final JsonMapper objectMapper;

    public PostCommandService(
            ApplicationEventPublisher events,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            PostRedisRepository postRedisRepository,
            JsonMapper jsonMapper) {
        this.events = events;
        this.localCache = localCache;
        this.redis = redis;
        this.postRedisRepository = postRedisRepository;
        this.objectMapper = jsonMapper;
    }

    public PostCommandResult createPost(CreatePostCommand cmd) {
        log.info("Creating post with id: {}", cmd.postId());

        // Generate timestamps
        LocalDateTime now = LocalDateTime.now();

        boolean isPublished = Boolean.TRUE.equals(cmd.published());
        LocalDateTime publishedAt = isPublished ? now : null;

        // Publish domain event (transactional - committed with transaction)
        PostCreatedEvent event = new PostCreatedEvent(
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                cmd.authorEmail(),
                isPublished,
                publishedAt,
                now,
                cmd.details(),
                cmd.tags());
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
                cmd.details(),
                cmd.tags());

        updateCaches(cmd.postId(), result);

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

        // Publish domain event
        PostUpdatedEvent event = new PostUpdatedEvent(
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                authorEmail,
                cmd.published() != null && cmd.published(),
                publishedAt,
                now,
                cmd.details(),
                cmd.tags());
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
                cmd.details(),
                cmd.tags());

        updateCaches(cmd.postId(), result);

        log.info("Post updated successfully: {}", cmd.postId());
        return result;
    }

    public void deletePost(Long postId) {
        log.info("Deleting post with id: {}", postId);

        // 1. Publish tombstone event
        events.publishEvent(new PostDeletedEvent(postId));

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
        try {
            redis.opsForValue().set("deleted:posts:" + postId, "1", Duration.ofSeconds(60));
        } catch (Exception e) {
            log.warn("Failed to mark post as deleted in Redis: {}", postId, e);
        }

        log.info("Post deleted successfully: {}", postId);
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
