package com.example.highrps.postcomment.domain;

import com.example.highrps.config.AppProperties;
import com.example.highrps.entities.PostCommentRedis;
import com.example.highrps.repository.jpa.PostCommentRepository;
import com.example.highrps.repository.redis.PostCommentRedisRepository;
import com.example.highrps.shared.ResourceNotFoundException;
import com.example.highrps.utility.CacheKeyGenerator;
import com.example.highrps.utility.RequestCoalescer;
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
    private final AppProperties appProperties;

    private volatile ReadOnlyKeyValueStore<String, PostCommentRequest> keyValueStore = null;
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public PostCommentQueryService(
            PostCommentRepository postCommentRepository,
            PostCommentMapper postCommentMapper,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            PostCommentRedisRepository postCommentRedisRepository,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            AppProperties appProperties) {
        this.postCommentRepository = postCommentRepository;
        this.postCommentMapper = postCommentMapper;
        this.localCache = localCache;
        this.redis = redis;
        this.postCommentRedisRepository = postCommentRedisRepository;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.requestCoalescer = new RequestCoalescer<>();
        this.appProperties = appProperties;
    }

    /**
     * Get comments by post ID.
     * Currently uses database-first approach. Future optimization: maintain
     * per-post index in Redis.
     */
    public List<PostCommentResult> getCommentsByPostId(Long postId) {
        return postCommentMapper.toResultList(postCommentRepository.findByPostId(postId));
    }

    /**
     * Get comment by ID with multi-layer cache pattern:
     * 1. Check tombstone in Redis
     * 2. Check local Caffeine cache
     * 3. Check Redis repository
     * 4. Check Kafka Streams state store
     * 5. Fallback to database
     */
    public PostCommentResult getCommentById(GetPostCommentQuery query) {
        var cacheKey = CacheKeyGenerator.generatePostCommentKey(
                query.postId(), query.commentId().id());

        // 1) Tombstone record check in Redis
        Boolean deleted = redis.hasKey("deleted:post-comments:" + cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            throw new ResourceNotFoundException(
                    "PostComment not found with id: " + query.commentId().id() + " for post: " + query.postId());
        }

        // 2) Local cache
        var cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.info(
                    "getCommentById: hit local cache for postId={} commentId={}",
                    query.postId(),
                    query.commentId().id());
            return postCommentMapper.fromJson(cached);
        }

        // 3) Redis materialized view
        Optional<PostCommentRedis> byId = postCommentRedisRepository.findById(cacheKey);
        if (byId.isPresent()) {
            log.info(
                    "getCommentById: hit redis repository for postId={} commentId={}",
                    query.postId(),
                    query.commentId().id());
            PostCommentResult result = postCommentMapper.toResultFromRedis(byId.get());
            var json = postCommentMapper.toJson(result);
            localCache.put(cacheKey, json);
            return result;
        }

        // 4) Kafka Streams state store (if enabled)
        if (appProperties.isKafkaStreamsEnabled()) {
            try {
                PostCommentRequest cachedRequest = requestCoalescer.subscribe(
                        cacheKey, () -> getKeyValueStore().get(cacheKey));
                if (cachedRequest != null) {
                    log.info(
                            "getCommentById: hit Kafka Streams store for postId={} commentId={}",
                            query.postId(),
                            query.commentId().id());
                    PostCommentResult result = postCommentMapper.toResultFromRequest(cachedRequest);
                    var json = postCommentMapper.toJson(result);
                    localCache.put(cacheKey, json);
                    try {
                        postCommentRedisRepository.save(postCommentMapper.toRedis(cachedRequest));
                    } catch (Exception re) {
                        log.warn(
                                "Failed to update Redis for postId {} commentId {} after Streams lookup",
                                query.postId(),
                                query.commentId().id(),
                                re);
                    }
                    return result;
                }
            } catch (Exception e) {
                log.error(
                        "Failed to query Kafka Streams store for postId: {} commentId: {}",
                        query.postId(),
                        query.commentId().id(),
                        e);
            }
        }

        // 5) Fallback to database
        var comment = postCommentRepository.getByIdAndPostId(query.commentId(), query.postId());
        PostCommentResult result = postCommentMapper.toResult(comment);

        // Update caches on cache miss (best-effort)
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
