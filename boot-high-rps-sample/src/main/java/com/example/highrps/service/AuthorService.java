package com.example.highrps.service;

import com.example.highrps.exception.ResourceNotFoundException;
import com.example.highrps.mapper.AuthorEntityToResponseMapper;
import com.example.highrps.mapper.AuthorRequestToResponseMapper;
import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.model.response.AuthorResponse;
import com.example.highrps.repository.AuthorRepository;
import com.github.benmanes.caffeine.cache.Cache;
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
public class AuthorService {

    private static final Logger log = LoggerFactory.getLogger(AuthorService.class);

    private final KafkaProducerService kafkaProducerService;
    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final AuthorRequestToResponseMapper authorRequestToResponseMapper;
    private final AuthorEntityToResponseMapper authorEntityToResponseMapper;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final AuthorRepository authorRepository;

    private volatile ReadOnlyKeyValueStore<String, AuthorRequest> keyValueStore = null;
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public AuthorService(
            KafkaProducerService kafkaProducerService,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            AuthorRequestToResponseMapper authorRequestToResponseMapper,
            AuthorEntityToResponseMapper authorEntityToResponseMapper,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            AuthorRepository authorRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.localCache = localCache;
        this.redis = redis;
        this.authorRequestToResponseMapper = authorRequestToResponseMapper;
        this.authorEntityToResponseMapper = authorEntityToResponseMapper;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.authorRepository = authorRepository;
    }

    public AuthorResponse saveAuthor(AuthorRequest newAuthorRequest) {
        // Publish a typed envelope for the 'author' entity so StreamsTopology can route it
        kafkaProducerService.publishEnvelope("author", newAuthorRequest.email(), newAuthorRequest);
        AuthorResponse authorResponse = authorRequestToResponseMapper.mapToAuthorResponse(newAuthorRequest);
        String json = AuthorResponse.toJson(authorResponse);
        localCache.put(newAuthorRequest.email(), json);
        return authorResponse;
    }

    public AuthorResponse updateAuthor(AuthorRequest newAuthorRequest) {
        String email = newAuthorRequest.email();
        kafkaProducerService.publishEnvelope("author", email, newAuthorRequest);

        AuthorResponse authorResponse = authorRequestToResponseMapper.mapToAuthorResponse(newAuthorRequest);
        String json = AuthorResponse.toJson(authorResponse);
        try {
            localCache.put(email, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for email: {}", email, e);
        }
        return authorResponse;
    }

    public void deleteAuthor(String email) {
        try {
            localCache.invalidate(email);
        } catch (Exception e) {
            log.warn("Failed to mark local cache deletion for email: {}", email, e);
        }

        try {
            redis.opsForValue().getAndDelete("authors:" + email);
        } catch (Exception e) {
            log.warn("Failed to delete redis key for email: {}", email, e);
        }

        try {
            // Publish tombstone to the per-entity aggregates topic so Streams/materializers see the delete
            kafkaProducerService.publishDeleteForEntity("author", email);
        } catch (Exception e) {
            log.warn("Failed to publish delete event for email: {}", email, e);
        }

        try {
            if (authorRepository.existsByEmail(email)) {
                log.info("Deleting author entity from DB for email: {}", email);
                authorRepository.deleteByEmail(email);
            }
        } catch (Exception e) {
            log.warn("Failed to query or delete DB entity for email: {}", email, e);
        }
    }

    public AuthorResponse findAuthorByEmail(String email) {
        var cached = localCache.getIfPresent(email);
        if (cached != null) {
            return AuthorResponse.fromJson(cached);
        }

        var raw = redis.opsForValue().get("authors:" + email);
        if (raw != null) {
            localCache.put(email, raw);
            return AuthorResponse.fromJson(raw);
        }

        try {
            AuthorRequest cachedRequest = getKeyValueStore().get(email);
            if (cachedRequest != null) {
                AuthorResponse response = authorRequestToResponseMapper.mapToAuthorResponse(cachedRequest);
                var json = AuthorResponse.toJson(response);
                localCache.put(email, json);
                redis.opsForValue().set("authors:" + email, json);
                return response;
            }
        } catch (Exception e) {
            log.error("Failed to query Kafka Streams store for email: {}", email, e);
        }

        return authorRepository
                .findByEmailAllIgnoreCase(email)
                .map(authorEntityToResponseMapper::convert)
                .map(response -> {
                    var json = AuthorResponse.toJson(response);
                    localCache.put(email, json);
                    redis.opsForValue().set("authors:" + email, json);
                    return response;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Author not found for email: " + email));
    }

    private ReadOnlyKeyValueStore<String, AuthorRequest> getKeyValueStore() {
        if (keyValueStore == null) {
            keyValueStoreLock.lock();
            try {
                if (keyValueStore == null) {
                    KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
                    Assert.notNull(kafkaStreams, () -> "Kafka Streams not initialized yet");
                    keyValueStore = kafkaStreams.store(
                            StoreQueryParameters.fromNameAndType("authors-store", QueryableStoreTypes.keyValueStore()));
                }
            } finally {
                keyValueStoreLock.unlock();
            }
        }
        return keyValueStore;
    }
}
