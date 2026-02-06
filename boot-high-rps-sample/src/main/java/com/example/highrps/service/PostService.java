package com.example.highrps.service;

import com.example.highrps.config.AppProperties;
import com.example.highrps.entities.PostRedis;
import com.example.highrps.exception.ResourceNotFoundException;
import com.example.highrps.mapper.PostRequestToResponseMapper;
import com.example.highrps.model.request.EventEnvelope;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.repository.jpa.PostRepository;
import com.example.highrps.repository.redis.PostRedisRepository;
import com.example.highrps.utility.RequestCoalescer;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
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

    // Lock used to initialize the Kafka Streams read-only store instead of using synchronized
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

    public PostResponse findPostByTitle(String title) {
        // 1a) Tombstone record check in Redis
        Boolean deleted = redis.hasKey("deleted:posts:" + title);
        if (Boolean.TRUE.equals(deleted)) {
            throw new ResourceNotFoundException("Post not found for title: " + title);
        }
        // 1b) Local cache
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
            NewPostRequest cachedRequest =
                    requestCoalescer.subscribe(title, () -> getKeyValueStore().get(title));
            if (cachedRequest != null) {
                PostResponse response = postRequestToResponseMapper.mapToPostResponse(cachedRequest);
                var json = PostResponse.toJson(response);
                localCache.put(title, json);
                try {
                    redis.opsForValue().set("posts:" + title, json);
                } catch (Exception re) {
                    log.warn("Failed to update Redis for title {} after Streams lookup", title, re);
                }
                return response;
            }
        } catch (Exception e) {
            log.error("Failed to query Kafka Streams store for title: {}", title, e);
        }

        throw new ResourceNotFoundException("Post not found for title: " + title);
    }

    public PostResponse saveOrUpdatePost(NewPostRequest newPostRequest) {
        String title = newPostRequest.title();
        if (newPostRequest.published() != null && newPostRequest.published() && newPostRequest.publishedAt() == null) {
            newPostRequest = newPostRequest.withPublishedAt(LocalDateTime.now());
        }

        // Build the response now so we can return it after successful publish
        PostResponse postResponse = postRequestToResponseMapper.mapToPostResponse(newPostRequest);
        String json = PostResponse.toJson(postResponse);

        // Publish envelope and wait for the send to complete. Only after a successful send
        // do we populate the local cache and return the response.
        var future = kafkaProducerService.publishEnvelope("post", title, newPostRequest);
        processFuture(future, title);

        // increment events counter after successful publish
        eventsPublishedCounter.increment();

        // Only populate local cache after successful publish. Cache update is best-effort.
        try {
            localCache.put(title, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for title after successful publish: {}", title, e);
        }

        // perform an eager repository write so batch/JPA processors can read repository-backed entries
        try {
            PostRedis pr = new PostRedis()
                    .setTitle(title)
                    .setContent(postResponse.content())
                    .setPublished(postResponse.published())
                    .setPublishedAt(postResponse.publishedAt());
            postRedisRepository.save(pr);
        } catch (Exception e) {
            log.warn("Eager redis repository write failed for title {} (listener will still populate redis)", title, e);
        }

        return postResponse;
    }

    public void deletePost(String title) {

        // 1) Publish tombstone to per-entity aggregates topic so Streams materialized KTable sees the delete
        try {
            var deleteForEntity = kafkaProducerService.publishDeleteForEntity("post", title);
            processFuture(deleteForEntity, title);
            log.info("deletePost: published tombstone for title={}", title);
            // increment tombstone counter after successful publish
            tombstonesPublishedCounter.increment();
        } catch (Exception e) {
            log.warn("Failed to publish delete event for title: {}", title, e);
        }

        // 2) Mark deletion in local cache so reads return absent immediately
        try {
            localCache.invalidate(title);
            log.info("deletePost: invalidated local cache for title={}", title);
        } catch (Exception e) {
            log.warn("Failed to mark local cache deletion for title: {}", title, e);
        }

        // 3) Remove from Redis so reads return absent immediately and batch processors don't re-populate from
        // repository

        // delete repository entry as well
        try {
            postRedisRepository.deleteById(title);
        } catch (Exception e) {
            log.warn("Failed to delete redis repository entry for title: {}", title, e);
        }

        // 4b) Mark deleted in a short-lived Redis set so batch processors skip re-inserts
        try {
            // Use per-title key so each deletion has independent TTL
            redis.opsForValue().set("deleted:posts:" + title, "1", Duration.ofSeconds(60));
        } catch (Exception ex) {
            log.warn("Failed to mark title {} in deleted:posts set", title, ex);
        }
    }

    private void processFuture(CompletableFuture<SendResult<String, EventEnvelope>> future, String title) {
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

    public boolean titleExists(String title) {
        try {
            Boolean deleted = redis.hasKey("deleted:posts:" + title);
            if (Boolean.TRUE.equals(deleted)) {
                return false;
            }
            if (localCache.getIfPresent(title) != null) {
                return true;
            }
            if (postRedisRepository.existsById(title)) {
                return true;
            }
            NewPostRequest cachedRequest =
                    requestCoalescer.subscribe(title, () -> getKeyValueStore().get(title));
            if (cachedRequest != null) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Cache/streams lookup failed for title {}, falling back to DB", title, e);
        }
        return postRepository.existsByTitle(title);
    }
}
