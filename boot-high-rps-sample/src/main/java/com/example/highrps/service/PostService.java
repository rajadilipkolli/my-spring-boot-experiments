package com.example.highrps.service;

import com.example.highrps.mapper.PostEntityToPostResponse;
import com.example.highrps.mapper.PostRequestToResponseMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.repository.PostRepository;
import com.github.benmanes.caffeine.cache.Cache;
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

    private volatile ReadOnlyKeyValueStore<String, NewPostRequest> keyValueStore = null;

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
        kafkaProducerService.publishEvent(newPostRequest);
        PostResponse postResponse = postRequestToResponseMapper.mapToPostResponse(newPostRequest);
        String json = PostResponse.toJson(postResponse);
        localCache.put(newPostRequest.title(), json);
        redis.opsForValue().set("posts:" + newPostRequest.title(), json);
        return postResponse;
    }

    public void deletePost(String title) {
        // 1) Mark deletion in local cache so reads return absent immediately
        try {
            localCache.invalidate(title);
        } catch (Exception e) {
            log.warn("Failed to mark local cache deletion for title: {}", title, e);
        }

        // 2) Remove from Redis
        try {
            redis.delete("posts:" + title);
        } catch (Exception e) {
            log.warn("Failed to delete redis key for title: {}", title, e);
        }

        // 3) Publish tombstone to Kafka events topic so Streams topology removes materialized entry
        try {
            kafkaProducerService.publishDelete(title);
        } catch (Exception e) {
            log.warn("Failed to publish delete event for title: {}", title, e);
        }

        // 4) Remove from persistent storage (if present)
        try {
            if (postRepository.existsByTitle(title)) {
                log.info("Deleting post entity from DB for title: {}", title);
                postRepository.deleteByTitle(title);
            }
        } catch (Exception e) {
            log.warn("Failed to query or delete DB entity for title: {}", title, e);
        }
    }

    public PostResponse findPostByTitle(String title) {
        // 1) Local cache
        var cached = localCache.getIfPresent(title);
        if (cached != null) {
            return PostResponse.fromJson(cached);
        }

        // 2) Redis materialized view
        var raw = redis.opsForValue().get("posts:" + title);
        if (raw != null) {
            localCache.put(title, raw);
            return PostResponse.fromJson(raw);
        }

        // 3) Fallback: interactive query against Kafka Streams `posts-store` (materialized KTable)
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
            // ignore and fall through to empty
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
                .orElse(null);
    }

    private ReadOnlyKeyValueStore<String, NewPostRequest> getKeyValueStore() {
        ReadOnlyKeyValueStore<String, NewPostRequest> store = keyValueStore;
        if (store == null) {
            synchronized (this) {
                store = keyValueStore;
                if (store == null) {
                    KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
                    Assert.notNull(kafkaStreams, () -> "Kafka Streams not initialized yet");
                    keyValueStore = store = kafkaStreams.store(
                            StoreQueryParameters.fromNameAndType("posts-store", QueryableStoreTypes.keyValueStore()));
                }
            }
        }
        return store;
    }
}
