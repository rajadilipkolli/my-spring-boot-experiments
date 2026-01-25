package com.example.highrps.service;

import com.example.highrps.config.AppProperties;
import com.example.highrps.exception.ResourceNotFoundException;
import com.example.highrps.mapper.AuthorRequestToResponseMapper;
import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.model.request.EventEnvelope;
import com.example.highrps.model.response.AuthorResponse;
import com.example.highrps.repository.AuthorRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
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
public class AuthorService {

    private static final Logger log = LoggerFactory.getLogger(AuthorService.class);

    private final KafkaProducerService kafkaProducerService;
    private final Cache<String, String> localCache;
    private final RedisTemplate<String, String> redis;
    private final AuthorRequestToResponseMapper authorRequestToResponseMapper;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private final AuthorRepository authorRepository;
    private final AppProperties appProperties;

    private volatile ReadOnlyKeyValueStore<String, AuthorRequest> keyValueStore = null;
    private final ReentrantLock keyValueStoreLock = new ReentrantLock();

    public AuthorService(
            KafkaProducerService kafkaProducerService,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            AuthorRequestToResponseMapper authorRequestToResponseMapper,
            StreamsBuilderFactoryBean kafkaStreamsFactory,
            AuthorRepository authorRepository,
            AppProperties appProperties) {
        this.kafkaProducerService = kafkaProducerService;
        this.localCache = localCache;
        this.redis = redis;
        this.authorRequestToResponseMapper = authorRequestToResponseMapper;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.authorRepository = authorRepository;
        this.appProperties = appProperties;
    }

    public AuthorResponse saveOrUpdateAuthor(AuthorRequest newAuthorRequest) {
        String email = newAuthorRequest.email();

        // Build the response now so we can return it after successful publish
        AuthorResponse authorResponse = authorRequestToResponseMapper.mapToAuthorResponse(newAuthorRequest);
        String json = AuthorResponse.toJson(authorResponse);

        // Publish envelope and wait for the send to complete. Only after a successful send
        // do we populate the local cache and return the response.
        var future = kafkaProducerService.publishEnvelope("author", email, newAuthorRequest);
        processFuture(email, future);

        // Only populate local cache after successful publish. Cache update is best-effort.
        try {
            localCache.put(email, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for email after successful publish: {}", email, e);
        }

        // perform an eager Redis write (listener remains source of truth for DB queue).
        try {
            redis.opsForValue().set("authors:" + email, json);
        } catch (Exception e) {
            log.warn("Eager redis write failed for email {} (listener will still populate redis)", email, e);
        }

        return authorResponse;
    }

    public void deleteAuthor(String email) {

        // Publish tombstone to the per-entity aggregates topic so Streams/materializers see the delete
        var deleteForEntity = kafkaProducerService.publishDeleteForEntity("author", email);
        processFuture(email, deleteForEntity);

        try {
            localCache.invalidate(email);
        } catch (Exception e) {
            log.warn("Failed to mark local cache deletion for email: {}", email, e);
        }

        try {
            String prev = redis.opsForValue().getAndDelete("authors:" + email);
            boolean existed = prev != null;
            log.debug("Deleted key for email {} presentBeforeDelete={}", email, existed);
        } catch (Exception e) {
            log.warn("Failed to delete redis key for email: {}", email, e);
        }

        // Mark deleted in a short-lived Redis set so batch processors skip re-inserts
        try {
            redis.opsForValue().set("deleted:authors:" + email, "1", Duration.ofSeconds(60));
        } catch (Exception ex) {
            log.warn("Failed to mark email {} in deleted:authors set", email, ex);
        }
    }

    private void processFuture(
            String email, CompletableFuture<SendResult<String, EventEnvelope>> sendResultCompletableFuture) {
        try {
            // Publish tombstone to the per-entity aggregates topic so Streams/materializers see the delete
            sendResultCompletableFuture.get(appProperties.getPublishTimeOutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            // Attempt to cancel the send if possible and surface a clear error
            try {
                sendResultCompletableFuture.cancel(true);
            } catch (Exception cancelEx) {
                log.warn("Failed to cancel publish future after timeout for email {}", email, cancelEx);
            }
            log.error(
                    "Timed out waiting for Kafka publish for email {} after {} ms",
                    email,
                    appProperties.getPublishTimeOutMs(),
                    te);
            throw new IllegalStateException("Timed out publishing author event for email " + email, te);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for Kafka publish for email {}", email, ie);
            throw new IllegalStateException("Interrupted while publishing author event for email " + email, ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause() == null ? ee : ee.getCause();
            log.error("Failed to publish author envelope for email {}", email, cause);
            throw new IllegalStateException("Failed to publish author event for email " + email, cause);
        } catch (Exception ex) {
            log.error("Unexpected error while publishing author envelope for email {}", email, ex);
            throw new IllegalStateException("Failed to publish author event for email " + email, ex);
        }
    }

    public AuthorResponse findAuthorByEmail(String email) {
        var cached = localCache.getIfPresent(email);
        if (cached != null) {
            log.info("findAuthorByEmail: hit local cache for email={}", email);
            return AuthorResponse.fromJson(cached);
        }

        try {
            var raw = redis.opsForValue().get("authors:" + email);
            if (raw != null) {
                log.info("findAuthorByEmail: hit redis for email={}", email);
                localCache.put(email, raw);
                return AuthorResponse.fromJson(raw);
            }
        } catch (Exception e) {
            log.warn("Failed to read from Redis for email: {}, falling back", email, e);
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

        throw new ResourceNotFoundException("Author not found for email: " + email);
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

    public boolean emailExists(String email) {
        return localCache.getIfPresent(email) != null
                || redis.opsForValue().get("authors:" + email) != null
                || getKeyValueStore().get(email) != null
                || authorRepository.existsByEmailIgnoreCase(email);
    }
}
