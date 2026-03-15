package com.example.highrps.post.api;

import com.example.highrps.entities.PostEntity;
import com.example.highrps.infrastructure.cache.RequestCoalescer;
import com.example.highrps.post.PostRedis;
import com.example.highrps.post.command.CreatePostCommand;
import com.example.highrps.post.command.PostCommandResult;
import com.example.highrps.post.command.PostCommandService;
import com.example.highrps.post.command.UpdatePostCommand;
import com.example.highrps.post.domain.PostResponse;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.post.mapper.PostCommandResultToPostResponse;
import com.example.highrps.post.mapper.PostRequestToResponseMapper;
import com.example.highrps.repository.jpa.PostRepository;
import com.example.highrps.repository.redis.PostRedisRepository;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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

    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final PostRequestToResponseMapper postRequestToResponseMapper;
    private final PostCommandResultToPostResponse postCommandResultToPostResponse;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final PostRepository postRepository;
    private final PostRedisRepository postRedisRepository;
    private final PostCommandService postCommandService;
    private final RequestCoalescer<NewPostRequest> requestCoalescer;
    private final Counter eventsPublishedCounter;
    private final Counter tombstonesPublishedCounter;

    private volatile ReadOnlyKeyValueStore<String, NewPostRequest> keyValueStore = null;

    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public PostService(
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            PostRequestToResponseMapper postRequestToResponseMapper,
            PostCommandResultToPostResponse postCommandResultToPostResponse,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            PostRepository postRepository,
            PostRedisRepository postRedisRepository,
            PostCommandService postCommandService,
            MeterRegistry meterRegistry) {
        this.localCache = localCache;
        this.redis = redis;
        this.postRequestToResponseMapper = postRequestToResponseMapper;
        this.postCommandResultToPostResponse = postCommandResultToPostResponse;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.postRepository = postRepository;
        this.postRedisRepository = postRedisRepository;
        this.postCommandService = postCommandService;
        this.requestCoalescer = new RequestCoalescer<>();

        this.eventsPublishedCounter = Counter.builder("posts.events.published")
                .description("Number of post events published to Kafka")
                .register(meterRegistry);
        this.tombstonesPublishedCounter = Counter.builder("posts.tombstones.published")
                .description("Number of post tombstone events published to Kafka")
                .register(meterRegistry);
    }

    public PostResponse findPostById(Long postId) {
        Boolean deleted = redis.hasKey("deleted:posts:" + postId);
        if (Boolean.TRUE.equals(deleted)) {
            throw new ResourceNotFoundException("Post not found for id: " + postId);
        }
        String cacheKey = String.valueOf(postId);
        var cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.info("findPostById: hit local cache for postId={}", postId);
            return PostResponse.fromJson(cached);
        }

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
        Assert.notNull(newPostRequest.postId(), () -> "postId must not be null");

        CreatePostCommand cmd = new CreatePostCommand(
                newPostRequest.postId(),
                newPostRequest.title(),
                newPostRequest.content(),
                newPostRequest.email(),
                newPostRequest.published() != null && newPostRequest.published(),
                newPostRequest.details(),
                newPostRequest.tags());

        PostCommandResult result = postCommandService.createPost(cmd);
        eventsPublishedCounter.increment();

        return postCommandResultToPostResponse.convert(result);
    }

    @Transactional
    public PostResponse updatePost(Long postId, NewPostRequest newPostRequest) {
        if (newPostRequest.postId() == null || !(Objects.equals(postId, newPostRequest.postId()))) {
            throw new ResourceNotFoundException("Path postId does not match request postId");
        }

        UpdatePostCommand cmd = new UpdatePostCommand(
                postId,
                newPostRequest.title(),
                newPostRequest.content(),
                newPostRequest.published(),
                newPostRequest.details(),
                newPostRequest.tags());

        PostCommandResult result = postCommandService.updatePost(cmd);
        eventsPublishedCounter.increment();

        return postCommandResultToPostResponse.convert(result);
    }

    @Transactional
    public void deletePostById(Long postId) {
        postCommandService.deletePost(postId);
        tombstonesPublishedCounter.increment();
    }

    public boolean existsByPostRefId(Long postRefId) {
        if (getCreatedAtByPostId(postRefId) != null) {
            return true;
        }
        return postRepository.existsByPostRefId(postRefId);
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
                .setAuthorEmail(newPostRequest.email())
                .setDetails(newPostRequest.details())
                .setTags(newPostRequest.tags());
        postRedis.setCreatedAt(newPostRequest.createdAt());
        postRedis.setModifiedAt(newPostRequest.modifiedAt());
        return postRedis;
    }
}
