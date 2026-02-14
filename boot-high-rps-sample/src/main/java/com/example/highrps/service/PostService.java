package com.example.highrps.service;

import com.example.highrps.config.AppProperties;
import com.example.highrps.entities.PostEntity;
import com.example.highrps.entities.PostRedis;
import com.example.highrps.mapper.PostRequestToResponseMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.repository.jpa.PostRepository;
import com.example.highrps.repository.redis.PostRedisRepository;
import com.example.highrps.shared.ResourceNotFoundException;
import com.example.highrps.utility.KafkaPublishHelper;
import com.example.highrps.utility.RequestCoalescer;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final KafkaProducerService kafkaProducerService;
    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final PostRequestToResponseMapper postRequestToResponseMapper;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final PostRepository postRepository;
    private final PostRedisRepository postRedisRepository;
    private final AppProperties appProperties;
    private final RequestCoalescer<NewPostRequest> requestCoalescer;
    private final Counter eventsPublishedCounter;
    private final Counter tombstonesPublishedCounter;

    private volatile ReadOnlyKeyValueStore<String, NewPostRequest> keyValueStore = null;

    // Lock used to initialize the Kafka Streams read-only store instead of using
    // synchronized
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public PostService(
            KafkaProducerService kafkaProducerService,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            PostRequestToResponseMapper postRequestToResponseMapper,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            PostRepository postRepository,
            PostRedisRepository postRedisRepository,
            AppProperties appProperties,
            MeterRegistry meterRegistry) {
        this.kafkaProducerService = kafkaProducerService;
        this.localCache = localCache;
        this.redis = redis;
        this.postRequestToResponseMapper = postRequestToResponseMapper;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.postRepository = postRepository;
        this.postRedisRepository = postRedisRepository;
        this.appProperties = appProperties;
        this.requestCoalescer = new RequestCoalescer<>();

        this.eventsPublishedCounter = Counter.builder("posts.events.published")
                .description("Number of post events published to Kafka")
                .register(meterRegistry);
        this.tombstonesPublishedCounter = Counter.builder("posts.tombstones.published")
                .description("Number of post tombstone events published to Kafka")
                .register(meterRegistry);
    }

    public PostResponse findPostById(Long postId) {
        // 1a) Tombstone record check in Redis to short-circuit reads for recently deleted posts.
        Boolean deleted = redis.hasKey("deleted:posts:" + postId);
        if (Boolean.TRUE.equals(deleted)) {
            throw new ResourceNotFoundException("Post not found for id: " + postId);
        }
        // 1b) Local cache
        String cacheKey = String.valueOf(postId);
        var cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.info("findPostById: hit local cache for postId={}", postId);
            return PostResponse.fromJson(cached);
        }

        // 2) Redis materialized view
        Optional<PostRedis> byId = postRedisRepository.findById(postId);
        if (byId.isPresent()) {
            log.info("findPostById: hit redis repository for postId={}", postId);
            PostResponse response = PostResponse.fromRedis(byId.get());
            var json = PostResponse.toJson(response);
            localCache.put(cacheKey, json);
            return response;
        }

        try {
            NewPostRequest cachedRequest = requestCoalescer.subscribe(
                    cacheKey, () -> getKeyValueStore().get(cacheKey));
            if (cachedRequest != null) {
                PostResponse response = postRequestToResponseMapper.mapToPostResponse(cachedRequest);
                var json = PostResponse.toJson(response);
                localCache.put(cacheKey, json);
                try {
                    postRedisRepository.save(toPostRedis(postId, cachedRequest));
                } catch (Exception re) {
                    log.warn("Failed to update Redis for postId {} after Streams lookup", postId, re);
                }
                return response;
            }
        } catch (Exception e) {
            log.error("Failed to query Kafka Streams store for postId: {}", postId, e);
        }

        throw new ResourceNotFoundException("Post not found for postId: " + postId);
    }

    @Transactional
    public PostResponse saveOrUpdatePost(NewPostRequest newPostRequest) {
        Objects.requireNonNull(newPostRequest.postId(), "postId must not be null");
        if (newPostRequest.published() != null && newPostRequest.published() && newPostRequest.publishedAt() == null) {
            newPostRequest = newPostRequest.withPublishedAt(LocalDateTime.now());
        }
        // Publish envelope and wait for the send to complete. Only after a successful
        // send
        // do we populate the local cache and return the response.
        String cacheKey = String.valueOf(newPostRequest.postId());
        var future = kafkaProducerService.publishEnvelope("post", cacheKey, newPostRequest);
        KafkaPublishHelper.awaitPublish(future, "post", cacheKey, appProperties.getPublishTimeOutMs());

        // increment events counter after successful publish
        eventsPublishedCounter.increment();

        // Build the response now so we can return it after successful publish
        PostResponse postResponse = postRequestToResponseMapper.mapToPostResponse(newPostRequest);
        String json = PostResponse.toJson(postResponse);
        // Only populate local cache after successful publish. Cache update is
        // best-effort.
        try {
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for postId : {} after successful publish", cacheKey, e);
        }

        // perform an eager repository write so batch/JPA processors can read
        // repository-backed entries
        try {
            postRedisRepository.save(toPostRedis(newPostRequest.postId(), newPostRequest));
        } catch (Exception e) {
            log.warn(
                    "Eager redis repository write failed for postId {} (listener will still populate redis)",
                    cacheKey,
                    e);
        }

        return postResponse;
    }

    /**
     * Update helper that validates the path postId matches the request postId,
     * sets timestamps, and delegates to {`@link` `#saveOrUpdatePost`}.
     */
    @Transactional
    public PostResponse updatePost(Long postId, NewPostRequest newPostRequest) {
        if (newPostRequest.postId() == null || !(Objects.equals(postId, newPostRequest.postId()))) {
            throw new ResourceNotFoundException("Path postId does not match request postId");
        }
        NewPostRequest withModifiedAt =
                newPostRequest.withTimestamps(getCreatedAtByPostId(newPostRequest.postId()), LocalDateTime.now());
        return saveOrUpdatePost(withModifiedAt);
    }

    @Transactional
    public void deletePostById(Long postId) {
        var cacheKey = String.valueOf(postId);
        // 1) Publish tombstone to per-entity aggregates topic so Streams materialized
        // KTable sees the delete
        try {
            var deleteForEntity = kafkaProducerService.publishDeleteForEntity("post", cacheKey);
            KafkaPublishHelper.awaitPublish(deleteForEntity, "posts", cacheKey, appProperties.getPublishTimeOutMs());
            log.info("deletePost: published tombstone for postId={}", cacheKey);
            // increment tombstone counter after successful publish
            tombstonesPublishedCounter.increment();
        } catch (Exception e) {
            log.warn("Failed to publish delete event for postId: {}", cacheKey, e);
        }

        // 2) Mark deletion in local cache so reads return absent immediately
        localCache.invalidate(cacheKey);
        log.info("deletePost: invalidated local cache for postId={}", cacheKey);

        // 3) Remove from Redis so reads return absent immediately and batch processors
        // don't re-populate from
        // repository
        try {
            postRedisRepository.deleteById(postId);
            log.info("deletePost: deleted redis repository entries for postId={}", postId);
        } catch (Exception e) {
            log.warn("Failed to delete redis repository entry for postId: {}", postId, e);
        }

        // 4b) Mark deleted in a short-lived Redis set so batch processors skip
        // re-inserts
        try {
            // Use per-postId cacheKey so each deletion has independent TTL
            redis.opsForValue().set("deleted:posts:" + cacheKey, "1", Duration.ofSeconds(60));
        } catch (Exception ex) {
            log.warn("Failed to mark postId {} in deleted:posts set", cacheKey, ex);
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

    private LocalDateTime getCreatedAtByPostId(Long postRefId) {
        var cacheKey = String.valueOf(postRefId);
        try {
            String localCacheIfPresent = localCache.getIfPresent(cacheKey);
            if (localCacheIfPresent != null) {
                return PostResponse.fromJson(localCacheIfPresent).createdAt();
            }
            Optional<PostRedis> postRedis = postRedisRepository.findById(postRefId);
            if (postRedis.isPresent()) {
                return postRedis.get().getCreatedAt();
            }
            NewPostRequest cachedRequest = requestCoalescer.subscribe(
                    cacheKey, () -> getKeyValueStore().get(cacheKey));
            if (cachedRequest != null) {
                return cachedRequest.createdAt();
            }
        } catch (Exception e) {
            log.warn("Cache/streams lookup failed for postRefId {}, falling back to DB", postRefId, e);
        }
        return postRepository
                .findByPostRefId(postRefId)
                .map(PostEntity::getCreatedAt)
                .orElse(null);
    }

    private PostRedis toPostRedis(Long postId, NewPostRequest newPostRequest) {
        PostRedis postRedis = new PostRedis()
                .setId(postId)
                .setTitle(newPostRequest.title())
                .setContent(newPostRequest.content())
                .setPublished(newPostRequest.published() != null && newPostRequest.published())
                .setPublishedAt(newPostRequest.publishedAt())
                .setAuthorEmail(newPostRequest.email());
        postRedis.setCreatedAt(newPostRequest.createdAt());
        postRedis.setModifiedAt(newPostRequest.modifiedAt());
        return postRedis;
    }
}
