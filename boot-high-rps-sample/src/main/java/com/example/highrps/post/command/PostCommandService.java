package com.example.highrps.post.command;

import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.PostRedis;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.post.domain.events.PostCreatedEvent;
import com.example.highrps.post.domain.events.PostDeletedEvent;
import com.example.highrps.post.domain.events.PostUpdatedEvent;
import com.example.highrps.post.query.PostQueryService;
import com.example.highrps.shared.AbstractCommandService;
import com.example.highrps.shared.AggregateOperationQueue;
import com.example.highrps.shared.ResourceConflictException;
import com.example.highrps.shared.config.AppProperties;
import com.example.highrps.shared.redis.DeletionMarkerHandler;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Command service for Post aggregate.
 * Handles all write operations (create, update, delete) and publishes domain
 * events.
 */
@Service
public class PostCommandService extends AbstractCommandService {

    private static final Logger log = LoggerFactory.getLogger(PostCommandService.class);

    private final Cache<String, String> localCache;
    private final PostRedisRepository postRedisRepository;
    private final JsonMapper jsonMapper;
    private final DeletionMarkerHandler deletionMarkerHandler;
    private final PostQueryService postQueryService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AggregateOperationQueue redisWriteQueue = new AggregateOperationQueue();

    /**
     * Creates a post command service with its event, cache, and persistence collaborators.
     *
     * @param kafkaTemplate publisher for post events
     * @param localCache local post cache
     * @param postRedisRepository Redis post repository
     * @param jsonMapper serializer for cached values
     * @param deletionMarkerHandler handler for deleted aggregates
     * @param postQueryService post read service
     * @param redisTemplate Redis operations used for reservations
     * @param appProperties application configuration
     */
    public PostCommandService(
            KafkaTemplate<String, Object> kafkaTemplate,
            Cache<String, String> localCache,
            PostRedisRepository postRedisRepository,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler,
            PostQueryService postQueryService,
            RedisTemplate<String, String> redisTemplate,
            AppProperties appProperties) {
        super(kafkaTemplate, appProperties.getKafka().getPublishTimeOutMs());
        this.localCache = localCache;
        this.postRedisRepository = postRedisRepository;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.postQueryService = postQueryService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Creates a post and publishes its creation event.
     *
     * @param cmd the post data to create
     * @return a future completed with the created post after the event is published
     * @throws ResourceConflictException if the post ID is already reserved or is detected by the query service
     */
    public CompletableFuture<PostCommandResult> createPost(CreatePostCommand cmd) {
        log.info("Creating post with id: {}", cmd.postId());

        String reservationKey = "reservation:post:" + cmd.postId();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(reservationKey, "1", Duration.ofMinutes(5));

        if (Boolean.FALSE.equals(acquired)) {
            throw new ResourceConflictException("Post already exists with id: " + cmd.postId());
        }

        boolean exists = false;
        try {
            exists = postQueryService.exists(cmd.postId());
        } catch (Exception e) {
            log.warn("Could not verify if post exists. Relying on distributed reservation.", e);
        }

        if (exists) {
            throw new ResourceConflictException("Post already exists with id: " + cmd.postId());
        }

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

        return executeCommand(
                        "posts-aggregates",
                        String.valueOf(cmd.postId()),
                        String.valueOf(cmd.postId()),
                        event,
                        result,
                        () -> updateCaches(cmd.postId(), result).join(),
                        "create post",
                        "Post")
                .whenComplete((res, err) -> {
                    if (err != null && !isPendingPublishFailure(err)) {
                        try {
                            redisTemplate.delete(reservationKey);
                        } catch (Exception e) {
                            log.warn(
                                    "Failed to clean up reservation key after creation failure: {}", reservationKey, e);
                        }
                    }
                });
    }

    /**
     * Updates a post and publishes the resulting aggregate event.
     *
     * @param cmd the replacement post data
     * @return a future completed with the updated post after the event is published
     */
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
                : getExistingTags(cmd.postId());

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

        return executeCommand(
                "posts-aggregates",
                String.valueOf(cmd.postId()),
                String.valueOf(cmd.postId()),
                event,
                result,
                () -> updateCaches(cmd.postId(), result),
                "update post",
                "Post");
    }

    /**
     * Deletes a post, invalidates its local cache entry, and records a Redis deletion marker.
     *
     * @param postId the identifier of the post to delete
     * @return a future completed after the deletion event and cache cleanup finish
     */
    public CompletableFuture<Void> deletePost(Long postId) {
        log.info("Deleting post with id: {}", postId);

        // 1. Publish tombstone event
        return executeCommand(
                "posts-aggregates",
                String.valueOf(postId),
                String.valueOf(postId),
                new PostDeletedEvent(postId),
                null, // Void result
                () -> {
                    // 2. Invalidate local cache
                    String cacheKey = String.valueOf(postId);
                    try {
                        localCache.invalidate(cacheKey);
                    } catch (Exception e) {
                        log.warn("Failed to invalidate local cache for post: {}", postId, e);
                    }

                    // 3. Mark deleted in Redis with TTL (prevents batch re-insertion)
                    deletionMarkerHandler.markDeleted(DeletionMarkerHandler.POST, String.valueOf(postId));
                },
                "delete post",
                "Post");
    }

    /**
     * Attempts to update the local read cache immediately, then schedules a best-effort Redis update.
     * Cache write failures are logged without being propagated to the command result.
     *
     * @param postId the post identifier used as the cache key
     * @param result the current post state to cache
     * @return a future completed after the queued Redis update finishes
     */
    private CompletableFuture<Void> updateCaches(Long postId, PostCommandResult result) {
        String cacheKey = String.valueOf(postId);

        // Update local cache
        try {
            String json = jsonMapper.writeValueAsString(result);
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for postId: {}", postId, e);
        }

        // Update Redis asynchronously to avoid blocking the hot path
        return redisWriteQueue.enqueue(String.valueOf(postId), () -> {
            if (deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST, String.valueOf(postId))) {
                log.debug("Skipping Redis update for deleted post: {}", postId);
                return CompletableFuture.completedFuture(null);
            }
            try {
                PostRedis redisEntity = new PostRedis()
                        .setId(postId)
                        .setTitle(result.title())
                        .setContent(result.content())
                        .setAuthorEmail(result.authorEmail())
                        .setPublished(result.published())
                        .setPublishedAt(result.publishedAt());

                if (result.details() != null) {
                    redisEntity.setDetails(result.details());
                }

                if (result.tags() != null) {
                    redisEntity.setTags(result.tags());
                }

                redisEntity.setCreatedAt(result.createdAt());
                redisEntity.setModifiedAt(result.modifiedAt());
                postRedisRepository.save(redisEntity);
                log.debug("Asynchronously updated Redis for post: {}", postId);
            } catch (Exception e) {
                log.error("Failed to asynchronously update Redis for post: {}", postId, e);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Reads the original creation time from Redis, falling back to the current time when unavailable.
     *
     * @param postId the post identifier
     * @return the stored creation time or the current time
     */
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

    /**
     * Reads the tags currently stored for a post.
     *
     * @param postId the post identifier
     * @return the stored tags, or an empty list when the post is absent
     */
    private List<TagResponse> getExistingTags(Long postId) {
        return postRedisRepository.findById(postId).map(PostRedis::getTags).orElse(List.of());
    }

    /**
     * Reads the author email currently stored for a post.
     *
     * @param postId the post identifier
     * @return the author email, or {@code null} when it cannot be read
     */
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
