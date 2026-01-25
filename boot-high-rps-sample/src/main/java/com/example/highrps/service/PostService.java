package com.example.highrps.service;

import com.example.highrps.config.AppProperties;
import com.example.highrps.exception.ResourceNotFoundException;
import com.example.highrps.mapper.PostEntityToPostResponse;
import com.example.highrps.mapper.PostRequestToResponseMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.repository.PostRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import org.springframework.util.Assert;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final KafkaProducerService kafkaProducerService;
    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final PostRequestToResponseMapper postRequestToResponseMapper;
    private final PostEntityToPostResponse postEntityToPostResponse;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final PostRepository postRepository;
    private final AppProperties appProperties;

    private volatile ReadOnlyKeyValueStore<String, NewPostRequest> keyValueStore = null;

    // Lock used to initialize the Kafka Streams read-only store instead of using synchronized
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public PostService(
            KafkaProducerService kafkaProducerService,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            PostRequestToResponseMapper postRequestToResponseMapper,
            PostEntityToPostResponse postEntityToPostResponse,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            PostRepository postRepository,
            AppProperties appProperties) {
        this.kafkaProducerService = kafkaProducerService;
        this.localCache = localCache;
        this.redis = redis;
        this.postRequestToResponseMapper = postRequestToResponseMapper;
        this.postEntityToPostResponse = postEntityToPostResponse;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.postRepository = postRepository;
        this.appProperties = appProperties;
    }

    public PostResponse saveOrUpdatePost(NewPostRequest newPostRequest) {
        String title = newPostRequest.title();
        if (newPostRequest.published() != null && newPostRequest.published()) {
            newPostRequest = newPostRequest.withPublishedAt(LocalDateTime.now());
        }

        // Build the response now so we can return it after successful publish
        PostResponse postResponse = postRequestToResponseMapper.mapToPostResponse(newPostRequest);
        String json = PostResponse.toJson(postResponse);

        // Publish envelope and wait for the send to complete. Only after a successful send
        // do we populate the local cache and return the response.
        var future = kafkaProducerService.publishEnvelope("post", title, newPostRequest);
        try {
            future.get(appProperties.getPublishTimeOutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            // Attempt to cancel the send if possible and surface a clear error
            try {
                future.cancel(true);
            } catch (Exception cancelEx) {
                log.warn("Failed to cancel publish future after timeout for title {}", title, cancelEx);
            }
            log.error(
                    "Timed out waiting for Kafka publish for title {} after {} ms",
                    title,
                    appProperties.getPublishTimeOutMs(),
                    te);
            throw new IllegalStateException("Timed out publishing post event for title " + title, te);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for Kafka publish for title {}", title, ie);
            throw new IllegalStateException("Interrupted while publishing post event for title " + title, ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause() == null ? ee : ee.getCause();
            log.error("Failed to publish post envelope for title {}", title, cause);
            throw new IllegalStateException("Failed to publish post event for title " + title, cause);
        } catch (Exception ex) {
            log.error("Unexpected error while publishing post envelope for title {}", title, ex);
            throw new IllegalStateException("Failed to publish post event for title " + title, ex);
        }

        // Only populate local cache after successful publish. Cache update is best-effort.
        try {
            localCache.put(title, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for title after successful publish: {}", title, e);
        }

        return postResponse;
    }

    public void deletePost(String title) {
        // 1) Mark deletion in local cache so reads return absent immediately
        try {
            localCache.invalidate(title);
            log.info("deletePost: invalidated local cache for title={}", title);
        } catch (Exception e) {
            log.warn("Failed to mark local cache deletion for title: {}", title, e);
        }

        // 2) Remove from Redis
        try {
            redis.opsForValue().getAndDelete("posts:" + title);
            log.info("deletePost: removed redis key for title={}", title);
        } catch (Exception e) {
            log.warn("Failed to delete redis key for title: {}", title, e);
        }

        // 3) Remove from persistent storage (if present) BEFORE publishing tombstone
        try {
            if (postRepository.existsByTitle(title)) {
                log.info("Deleting post entity from DB for title: {}", title);
                postRepository.deleteByTitle(title);
            } else {
                log.info("deletePost: no DB entity found for title={}", title);
            }
        } catch (Exception e) {
            log.warn("Failed to query or delete DB entity for title: {}", title, e);
        }

        // 4) Publish tombstone to per-entity aggregates topic so Streams materialized KTable sees the delete
        try {
            kafkaProducerService.publishDeleteForEntity("post", title);
            log.info("deletePost: published tombstone for title={}", title);
        } catch (Exception e) {
            log.warn("Failed to publish delete event for title: {}", title, e);
        }

        // 4b) Mark deleted in a short-lived Redis set so batch processors skip re-inserts
        try {
            // Use per-title key so each deletion has independent TTL
            redis.opsForValue().set("deleted:posts:" + title, "1", Duration.ofSeconds(60));
        } catch (Exception ex) {
            log.warn("Failed to mark title {} in deleted:posts set", title, ex);
        }
    }

    public PostResponse findPostByTitle(String title) {
        // 1) Local cache
        var cached = localCache.getIfPresent(title);
        if (cached != null) {
            log.info("findPostByTitle: hit local cache for title={}", title);
            return PostResponse.fromJson(cached);
        }

        // 2) Redis materialized view
        var raw = redis.opsForValue().get("posts:" + title);
        if (raw != null) {
            log.info("findPostByTitle: hit redis for title={}", title);
            localCache.put(title, raw);
            return PostResponse.fromJson(raw);
        }

        try {
            NewPostRequest cachedRequest = getKeyValueStore().get(title);
            if (cachedRequest != null) {
                PostResponse response = postRequestToResponseMapper.mapToPostResponse(cachedRequest);
                var json = PostResponse.toJson(response);
                localCache.put(title, json);
                redis.opsForValue().set("posts:" + title, json);
                return response;
            }
        } catch (Exception e) {
            log.error("Failed to query Kafka Streams store for title: {}", title, e);
        }

        return postRepository
                .findByTitle(title)
                .map(postEntityToPostResponse::convert)
                .map(response -> {
                    var json = PostResponse.toJson(response);
                    localCache.put(title, json);
                    redis.opsForValue().set("posts:" + title, json);
                    return response;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Post not found for title: " + title));
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

    public boolean isTitleAvailable(String title) {
        return localCache.getIfPresent(title) != null
                || redis.opsForValue().get("posts:" + title) != null
                || getKeyValueStore().get(title) != null
                || postRepository.existsByTitle(title);
    }
}
