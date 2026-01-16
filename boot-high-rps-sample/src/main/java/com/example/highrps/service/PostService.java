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
        return postRequestToResponseMapper.toResponse(newPostRequest);
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
                PostResponse response = postRequestToResponseMapper.toResponse(cachedRequest);
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
                .orElseGet(PostResponse::new);
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
