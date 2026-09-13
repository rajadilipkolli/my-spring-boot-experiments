package com.example.highrps.post.query;

import com.example.highrps.infrastructure.cache.RequestCoalescer;
import com.example.highrps.post.domain.*;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.shared.ResourceNotFoundException;
import com.example.highrps.shared.redis.DeletionMarkerHandler;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
import java.util.Optional;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Query service for Post aggregate.
 * Handles all read operations with multi-layer caching strategy.
 */
@Service
@Transactional(readOnly = true)
public class PostQueryService {

    private static final Logger log = LoggerFactory.getLogger(PostQueryService.class);

    private final Cache<String, String> localCache;
    private final PostRedisRepository postRedisRepository;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final DeletionMarkerHandler deletionMarkerHandler;
    private final RequestCoalescer<NewPostRequest> requestCoalescer;
    private final JsonMapper jsonMapper;
    private final PostRepository postRepository;

    public PostQueryService(
            Cache<String, String> localCache,
            PostRedisRepository postRedisRepository,
            PostRepository postRepository,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.localCache = localCache;
        this.postRedisRepository = postRedisRepository;
        this.postRepository = postRepository;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.requestCoalescer = new RequestCoalescer<>();
    }

    /**
     * Resolves a post from the read caches, stream state, or database and warms faster cache layers when possible.
     *
     * @param query the post ID query
     * @return the matching post serialized as JSON
     * @throws ResourceNotFoundException if the post is marked as deleted or cannot be found
     */
    public String getPost(PostQuery query) {
        Long postId = query.postId();
        log.debug("Querying post with id: {}", postId);

        // 1. Check tombstone (deleted posts)
        if (deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST, String.valueOf(postId))) {
            throw new ResourceNotFoundException("Post not found for id: " + postId);
        }

        // 2. Local cache (fastest)
        String cacheKey = String.valueOf(postId);
        String cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("Hit local cache for postId: {}", postId);
            return cached;
        }

        // 3. Redis materialized view (fast)
        Optional<PostRedis> redisPost = postRedisRepository.findById(postId);
        if (redisPost.isPresent()) {
            log.debug("Hit Redis for postId: {}", postId);
            PostProjection projection = fromRedis(redisPost.get());
            // Warm local cache and return JSON
            try {
                String jsonStr = jsonMapper.writeValueAsString(projection);
                localCache.put(cacheKey, jsonStr);
                return jsonStr;

            } catch (Exception e) {
                log.warn("Failed to serialize to JSON", e);
                throw new RuntimeException("Serialization error", e);
            }
        }

        // 4. Kafka Streams state store (recent events)
        NewPostRequest streamsData = null;
        try {
            streamsData = requestCoalescer.subscribe(cacheKey, () -> {
                ReadOnlyKeyValueStore<String, NewPostRequest> store = getKeyValueStore();
                return store != null ? store.get(cacheKey) : null;
            });
        } catch (Exception e) {
            log.debug("Failed to query Kafka Streams for postId: {} - {}", postId, e.getMessage());
        }

        if (streamsData != null) {
            log.debug("Hit Kafka Streams for postId: {}", postId);
            PostProjection projection = fromNewPostRequest(streamsData);

            // Warm both caches and return JSON
            try {
                String jsonStr = jsonMapper.writeValueAsString(projection);
                localCache.put(cacheKey, jsonStr);

                PostRedis redisEntity = toRedis(streamsData, postId);
                postRedisRepository.save(redisEntity);
                return jsonStr;

            } catch (Exception e) {
                log.warn("Failed to warm caches from Streams", e);
                throw new RuntimeException("Serialization error", e);
            }
        }

        // 5. Database fallback
        return postRepository
                .findByPostRefId(postId)
                .map(entity -> {
                    log.debug("Hit DB for postId: {}", postId);
                    PostProjection projection = fromEntity(entity);
                    try {
                        String jsonStr = jsonMapper.writeValueAsString(projection);
                        localCache.put(cacheKey, jsonStr);
                        return jsonStr;

                    } catch (Exception e) {
                        throw new RuntimeException("Serialization error", e);
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException("Post not found for id: " + postId));
    }

    public boolean exists(Long postId) {
        try {
            getPost(new PostQuery(postId));
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private ReadOnlyKeyValueStore<String, NewPostRequest> getKeyValueStore() {
        KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
        if (kafkaStreams == null || kafkaStreams.state() != KafkaStreams.State.RUNNING) {
            return null;
        }
        return kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("posts-store", QueryableStoreTypes.keyValueStore()));
    }

    private PostProjection fromEntity(PostEntity entity) {
        return new PostProjection(
                entity.getPostRefId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAuthorEntity().getEmail(),
                entity.isPublished(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.getDetails() == null
                        ? null
                        : new PostDetailsResponse(
                                entity.getDetails().getDetailsKey(),
                                entity.getDetails().getCreatedAt(),
                                entity.getDetails().getCreatedBy()),
                List.of());
    }

    private PostProjection fromRedis(PostRedis postRedis) {
        return new PostProjection(
                postRedis.getId(),
                postRedis.getTitle(),
                postRedis.getContent(),
                postRedis.getAuthorEmail(),
                postRedis.isPublished(),
                postRedis.getPublishedAt(),
                postRedis.getCreatedAt(),
                postRedis.getModifiedAt(),
                postRedis.getDetails(),
                postRedis.getTags());
    }

    private PostProjection fromNewPostRequest(NewPostRequest request) {
        return new PostProjection(
                request.postId(),
                request.title(),
                request.content(),
                request.email(),
                request.published() != null && request.published(),
                request.publishedAt(),
                request.createdAt(),
                request.modifiedAt(),
                request.details(),
                request.tags());
    }

    private PostRedis toRedis(NewPostRequest request, Long postId) {
        return new PostRedis()
                .setId(postId)
                .setTitle(request.title())
                .setContent(request.content())
                .setPublished(request.published() != null && request.published())
                .setPublishedAt(request.publishedAt())
                .setAuthorEmail(request.email())
                .setCreatedAt(request.createdAt())
                .setModifiedAt(request.modifiedAt())
                .setDetails(request.details())
                .setTags(request.tags());
    }
}
