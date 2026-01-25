package com.example.highrps.service;

import com.example.highrps.exception.ResourceNotFoundException;
import com.example.highrps.mapper.PostEntityToPostResponse;
import com.example.highrps.mapper.PostRequestToResponseMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.repository.PostRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.batch.queue-key:events:queue}")
    private String batchQueueKey;

    // New flag: control whether deletePost blocks waiting for Kafka Streams tombstone
    @Value("${app.wait-for-tombstone:false}")
    private boolean waitForTombstone;

    // Executor for asynchronous tombstone waits when blocking is disabled
    private final ExecutorService tombstoneWaitExecutor = Executors.newCachedThreadPool();

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
            PostRepository postRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.localCache = localCache;
        this.redis = redis;
        this.postRequestToResponseMapper = postRequestToResponseMapper;
        this.postEntityToPostResponse = postEntityToPostResponse;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.postRepository = postRepository;
    }

    public PostResponse savePost(NewPostRequest newPostRequest) {
        if (newPostRequest.published() != null && newPostRequest.published()) {
            newPostRequest = newPostRequest.withPublishedAt(LocalDateTime.now());
        }
        NewPostRequest finalNewPostRequest = newPostRequest;
        kafkaProducerService
                .publishEnvelope("post", newPostRequest.title(), newPostRequest)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish post envelope for title {}", finalNewPostRequest.title(), ex);
                    }
                });
        PostResponse postResponse = postRequestToResponseMapper.mapToPostResponse(newPostRequest);
        String json = PostResponse.toJson(postResponse);
        localCache.put(newPostRequest.title(), json);
        return postResponse;
    }

    public PostResponse updatePost(NewPostRequest newPostRequest) {
        String title = newPostRequest.title();
        if (newPostRequest.published() != null && newPostRequest.published() && newPostRequest.publishedAt() == null) {
            newPostRequest = newPostRequest.withPublishedAt(LocalDateTime.now());
        }
        // Publish event first so Streams + Aggregates handle materialized view
        kafkaProducerService
                .publishEnvelope("post", newPostRequest.title(), newPostRequest)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish post envelope for title {}", title, ex);
                    }
                });

        // Update local cache and redis to reflect new state immediately
        PostResponse postResponse = postRequestToResponseMapper.mapToPostResponse(newPostRequest);
        String json = PostResponse.toJson(postResponse);
        try {
            localCache.put(title, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for title: {}", title, e);
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
            redis.opsForSet().add("deleted:posts", title);
            redis.expire("deleted:posts", Duration.ofSeconds(60));
        } catch (Exception ex) {
            log.warn("Failed to mark title {} in deleted:posts set", title, ex);
        }

        // 5) Wait briefly for the Kafka Streams materialized store to apply the tombstone so interactive queries
        // (and subsequent GETs) do not return stale results. This keeps the API behavior consistent for tests.
        if (waitForTombstone) {
            try {
                ReadOnlyKeyValueStore<String, NewPostRequest> store = getKeyValueStore();
                long deadline = System.currentTimeMillis() + 5000; // wait up to 5s
                while (System.currentTimeMillis() < deadline) {
                    if (store.get(title) == null) {
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            } catch (Exception e) {
                log.warn("Failed to wait for Kafka Streams store to apply tombstone for title: {}", title, e);
            }
        } else {
            // Run the same polling logic asynchronously so we do not block production request threads.
            tombstoneWaitExecutor.submit(() -> {
                try {
                    ReadOnlyKeyValueStore<String, NewPostRequest> store = getKeyValueStore();
                    long deadline = System.currentTimeMillis() + 5000; // try up to 5s in background
                    while (System.currentTimeMillis() < deadline) {
                        try {
                            if (store.get(title) == null) {
                                return; // success
                            }
                        } catch (Exception inner) {
                            // If store.get throws, log and bail — we don't want to silently swallow in production
                            log.warn(
                                    "Background wait: failed to query Kafka Streams store for title: {}", title, inner);
                            return;
                        }
                        Thread.sleep(200);
                    }
                    log.warn(
                            "Background wait: kafka streams store did not apply tombstone within timeout for title={}",
                            title);
                } catch (Exception e) {
                    log.warn(
                            "Background wait: unexpected exception while waiting for tombstone for title: {}",
                            title,
                            e);
                }
            });
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

        // 3) Check authoritative persistent storage (DB) first to ensure deletes are visible promptly
        var fromDb = postRepository.findByTitle(title);
        if (fromDb.isPresent()) {
            log.info("findPostByTitle: hit DB for title={}", title);
            var response = postEntityToPostResponse.convert(fromDb.get());
            var json = PostResponse.toJson(response);
            localCache.put(title, json);
            redis.opsForValue().set("posts:" + title, json);
            return response;
        }

        log.info("findPostByTitle: nothing found for title={}, throwing ResourceNotFound", title);

        // NOTE: we intentionally do not consult Kafka Streams interactive store here because it may be stale
        // compared to the authoritative DB. Using DB + Redis + local cache ensures deletes are visible
        // immediately after persistent deletion and avoids test flakiness due to Streams propagation delays.

        throw new ResourceNotFoundException("Post not found for title: " + title);
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
}
