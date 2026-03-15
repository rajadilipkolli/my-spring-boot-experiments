package com.example.highrps.postcomment.query;

import com.example.highrps.entities.PostCommentRedis;
import com.example.highrps.infrastructure.cache.CacheKeyGenerator;
import com.example.highrps.infrastructure.cache.RequestCoalescer;
import com.example.highrps.postcomment.command.PostCommentCommandResult;
import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import com.example.highrps.repository.jpa.PostCommentRepository;
import com.example.highrps.repository.redis.PostCommentRedisRepository;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
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

/**
 * Query service for PostComment aggregate.
 * Handles all read operations with multi-layer caching.
 */
@Service
@Transactional(readOnly = true)
public class PostCommentQueryService {

    private static final Logger log = LoggerFactory.getLogger(PostCommentQueryService.class);

    private final PostCommentRepository postCommentRepository;
    private final PostCommentMapper postCommentMapper;
    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final PostCommentRedisRepository postCommentRedisRepository;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final RequestCoalescer<PostCommentRequest> requestCoalescer;

    private volatile ReadOnlyKeyValueStore<String, PostCommentRequest> keyValueStore = null;
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public PostCommentQueryService(
            PostCommentRepository postCommentRepository,
            PostCommentMapper postCommentMapper,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            PostCommentRedisRepository postCommentRedisRepository,
            StreamsBuilderFactoryBean kafkaStreamsFactory) {
        this.postCommentRepository = postCommentRepository;
        this.postCommentMapper = postCommentMapper;
        this.localCache = localCache;
        this.redis = redis;
        this.postCommentRedisRepository = postCommentRedisRepository;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.requestCoalescer = new RequestCoalescer<>();
    }

    /**
     * Get comments by post ID.
     * Checks Redis first (populated eagerly by command service), then falls back to JPA.
     */
    public List<PostCommentCommandResult> getCommentsByPostId(Long postId) {
        // 1. Try Redis first (eager writes land here immediately)
        try {
            var redisComments = postCommentRedisRepository.findByPostId(postId);
            if (redisComments != null && !redisComments.isEmpty()) {
                log.debug("getCommentsByPostId: hit Redis for postId={}, count={}", postId, redisComments.size());
                return redisComments.stream()
                        .map(postCommentMapper::toResultFromRedis)
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to query Redis for postId={}, falling back to JPA", postId, e);
        }

        // 2. Fallback to JPA
        return postCommentMapper.toResultList(postCommentRepository.findByPostRefId(postId));
    }

    /**
     * Get comment by ID with multi-layer cache pattern.
     */
    public PostCommentCommandResult getCommentById(GetPostCommentQuery query) {
        var cacheKey = CacheKeyGenerator.generatePostCommentKey(
                query.postId(), query.commentId().id());

        // 1. Check tombstone
        Boolean deleted = redis.hasKey("deleted:post-comments:" + cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            throw new ResourceNotFoundException(
                    "PostComment not found with id: " + query.commentId().id() + " for post: " + query.postId());
        }

        // 2. Local cache
        var cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug(
                    "Hit local cache for postId={} commentId={}",
                    query.postId(),
                    query.commentId().id());
            return postCommentMapper.fromJson(cached);
        }

        // 3. Redis materialized view
        Optional<PostCommentRedis> byId = postCommentRedisRepository.findById(cacheKey);
        if (byId.isPresent()) {
            log.debug(
                    "Hit Redis for postId={} commentId={}",
                    query.postId(),
                    query.commentId().id());
            PostCommentCommandResult result = postCommentMapper.toResultFromRedis(byId.get());
            // Warm local cache
            try {
                var json = postCommentMapper.toJson(result);
                localCache.put(cacheKey, json);
            } catch (Exception e) {
                log.warn("Failed to warm local cache", e);
            }
            return result;
        }

        // 4. Kafka Streams state store
        try {
            PostCommentRequest cachedRequest = requestCoalescer.subscribe(
                    cacheKey, () -> getKeyValueStore().get(cacheKey));
            if (cachedRequest != null) {
                log.debug(
                        "Hit Kafka Streams for postId={} commentId={}",
                        query.postId(),
                        query.commentId().id());
                PostCommentCommandResult result = postCommentMapper.toResultFromRequest(cachedRequest);

                // Warm both caches
                try {
                    var json = postCommentMapper.toJson(result);
                    localCache.put(cacheKey, json);
                    postCommentRedisRepository.save(postCommentMapper.toRedis(cachedRequest));
                } catch (Exception e) {
                    log.warn("Failed to warm caches from Streams", e);
                }

                return result;
            }
        } catch (Exception e) {
            log.error(
                    "Failed to query Kafka Streams for postId: {} commentId: {}",
                    query.postId(),
                    query.commentId().id(),
                    e);
        }

        // 5. Fallback to database
        var comment = postCommentRepository.getByCommentRefIdAndPostRefId(query.commentId(), query.postId());
        PostCommentCommandResult result = postCommentMapper.toResult(comment);

        // Update caches on cache miss
        try {
            var json = postCommentMapper.toJson(result);
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache after database lookup", e);
        }

        return result;
    }

    private ReadOnlyKeyValueStore<String, PostCommentRequest> getKeyValueStore() {
        if (keyValueStore == null) {
            keyValueStoreLock.lock();
            try {
                if (keyValueStore == null) {
                    KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
                    Assert.notNull(kafkaStreams, () -> "Kafka Streams not initialized yet");

                    // Wait for streams to be RUNNING
                    int attempts = 0;
                    while (kafkaStreams.state() != KafkaStreams.State.RUNNING && attempts < 30) {
                        log.info("Waiting for Kafka Streams to be RUNNING. Current state: {}", kafkaStreams.state());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted while waiting for Kafka Streams", e);
                        }
                        attempts++;
                    }

                    if (kafkaStreams.state() != KafkaStreams.State.RUNNING) {
                        throw new IllegalStateException(
                                "Kafka Streams did not reach RUNNING state in time. Current: " + kafkaStreams.state());
                    }

                    keyValueStore = kafkaStreams.store(StoreQueryParameters.fromNameAndType(
                            "post-comments-store", QueryableStoreTypes.keyValueStore()));
                }
            } finally {
                keyValueStoreLock.unlock();
            }
        }
        return keyValueStore;
    }
}
