package com.example.highrps.author.query;

import com.example.highrps.author.AuthorRequest;
import com.example.highrps.entities.AuthorRedis;
import com.example.highrps.infrastructure.cache.RequestCoalescer;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
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
import tools.jackson.databind.json.JsonMapper;

/**
 * Query service for Author aggregate.
 * Handles all read operations with multi-layer caching.
 */
@Service
@Transactional(readOnly = true)
public class AuthorQueryService {

    private static final Logger log = LoggerFactory.getLogger(AuthorQueryService.class);

    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final AuthorRedisRepository authorRedisRepository;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final RequestCoalescer<AuthorRequest> requestCoalescer;
    private final JsonMapper jsonMapper;

    private volatile ReadOnlyKeyValueStore<String, AuthorRequest> keyValueStore = null;
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public AuthorQueryService(
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            AuthorRedisRepository authorRedisRepository,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            JsonMapper jsonMapper) {
        this.localCache = localCache;
        this.redis = redis;
        this.authorRedisRepository = authorRedisRepository;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.jsonMapper = jsonMapper;
        this.requestCoalescer = new RequestCoalescer<>();
    }

    public AuthorProjection getAuthor(AuthorQuery query) {
        String email = query.email();
        log.debug("Querying author with email: {}", email);

        // 1. Check tombstone
        Boolean deleted = redis.hasKey("deleted:authors:" + email);
        if (Boolean.TRUE.equals(deleted)) {
            throw new ResourceNotFoundException("Author not found for email: " + email);
        }

        // 2. Local cache
        String cacheKey = email.toLowerCase();
        String cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("Hit local cache for email: {}", email);
            return parseProjection(cached);
        }

        // 3. Redis materialized view
        Optional<AuthorRedis> redisAuthor = authorRedisRepository.findById(email);
        if (redisAuthor.isPresent()) {
            log.debug("Hit Redis for email: {}", email);
            AuthorProjection projection = fromRedis(redisAuthor.get());
            // Warm local cache
            try {
                String json = jsonMapper.writeValueAsString(projection);
                localCache.put(cacheKey, json);
            } catch (Exception e) {
                log.warn("Failed to warm local cache", e);
            }
            return projection;
        }

        // 4. Kafka Streams state store
        try {
            AuthorRequest streamsData = requestCoalescer.subscribe(
                    cacheKey, () -> getKeyValueStore().get(cacheKey));

            if (streamsData != null) {
                log.debug("Hit Kafka Streams for email: {}", email);
                AuthorProjection projection = fromAuthorRequest(streamsData);

                // Warm both caches
                try {
                    String json = jsonMapper.writeValueAsString(projection);
                    localCache.put(cacheKey, json);

                    AuthorRedis redisEntity = toRedis(streamsData);
                    authorRedisRepository.save(redisEntity);
                } catch (Exception e) {
                    log.warn("Failed to warm caches from Streams", e);
                }

                return projection;
            }
        } catch (Exception e) {
            log.error("Failed to query Kafka Streams for email: {}", email, e);
        }

        throw new ResourceNotFoundException("Author not found for email: " + email);
    }

    public boolean exists(String email) {
        try {
            getAuthor(new AuthorQuery(email));
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
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

    private AuthorProjection parseProjection(String json) {
        try {
            return jsonMapper.readValue(json, AuthorProjection.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AuthorProjection from JSON", e);
        }
    }

    private AuthorProjection fromRedis(AuthorRedis authorRedis) {
        return new AuthorProjection(
                authorRedis.getEmail(), authorRedis.getFirstName(), authorRedis.getLastName(), authorRedis.getMobile());
    }

    private AuthorProjection fromAuthorRequest(AuthorRequest request) {
        return new AuthorProjection(request.email(), request.firstName(), request.lastName(), request.mobile());
    }

    private AuthorRedis toRedis(AuthorRequest request) {
        return new AuthorRedis()
                .setEmail(request.email())
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setMobile(request.mobile());
    }
}
