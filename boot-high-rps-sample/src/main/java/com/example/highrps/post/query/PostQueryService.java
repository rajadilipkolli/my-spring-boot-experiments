package com.example.highrps.post.query;

import com.example.highrps.infrastructure.cache.RequestCoalescer;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.PostRedis;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.repository.redis.PostRedisRepository;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
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

    private volatile ReadOnlyKeyValueStore<String, NewPostRequest> keyValueStore = null;
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public PostQueryService(
            Cache<String, String> localCache,
            PostRedisRepository postRedisRepository,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.localCache = localCache;
        this.postRedisRepository = postRedisRepository;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.requestCoalescer = new RequestCoalescer<>();
    }

    public PostProjection getPost(PostQuery query) {
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
            return parseProjection(cached);
        }

        // 3. Redis materialized view (fast)
        Optional<PostRedis> redisPost = postRedisRepository.findById(postId);
        if (redisPost.isPresent()) {
            log.debug("Hit Redis for postId: {}", postId);
            PostProjection projection = fromRedis(redisPost.get());
            // Warm local cache
            try {
                String json = jsonMapper.writeValueAsString(projection);
                localCache.put(cacheKey, json);
            } catch (Exception e) {
                log.warn("Failed to warm local cache", e);
            }
            return projection;
        }

        // 4. Kafka Streams state store (recent events)
        try {
            NewPostRequest streamsData = requestCoalescer.subscribe(
                    cacheKey, () -> getKeyValueStore().get(cacheKey));

            if (streamsData != null) {
                log.debug("Hit Kafka Streams for postId: {}", postId);
                PostProjection projection = fromNewPostRequest(streamsData);

                // Warm both caches
                try {
                    String json = jsonMapper.writeValueAsString(projection);
                    localCache.put(cacheKey, json);

                    PostRedis redisEntity = toRedis(streamsData, postId);
                    postRedisRepository.save(redisEntity);
                } catch (Exception e) {
                    log.warn("Failed to warm caches from Streams", e);
                }

                return projection;
            }
        } catch (Exception e) {
            log.error("Failed to query Kafka Streams for postId: {}", postId, e);
        }

        // Not found in any cache
        throw new ResourceNotFoundException("Post not found for id: " + postId);
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
        if (keyValueStore == null) {
            keyValueStoreLock.lock();
            try {
                if (keyValueStore == null) {
                    KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
                    Assert.notNull(kafkaStreams, () -> "Kafka Streams not initialized yet");
                    keyValueStore = kafkaStreams.store(
                            StoreQueryParameters.fromNameAndType("posts-store", QueryableStoreTypes.keyValueStore()));
                }
            } finally {
                keyValueStoreLock.unlock();
            }
        }
        return keyValueStore;
    }

    private PostProjection parseProjection(String json) {
        try {
            return jsonMapper.readValue(json, PostProjection.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse PostProjection from JSON", e);
        }
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
