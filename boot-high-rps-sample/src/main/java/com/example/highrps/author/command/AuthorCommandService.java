package com.example.highrps.author.command;

import com.example.highrps.author.domain.AuthorRedis;
import com.example.highrps.author.domain.AuthorRedisRepository;
import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.example.highrps.author.domain.events.AuthorDeletedEvent;
import com.example.highrps.author.domain.events.AuthorUpdatedEvent;
import com.example.highrps.author.query.AuthorProjection;
import com.example.highrps.author.query.AuthorQuery;
import com.example.highrps.author.query.AuthorQueryService;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Command service for Author aggregate.
 * Handles all write operations and publishes domain events.
 */
@Service
public class AuthorCommandService {

    private static final Logger log = LoggerFactory.getLogger(AuthorCommandService.class);
    private static final java.util.concurrent.Executor VIRTUAL_EXECUTOR =
            java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Cache<String, String> localCache;
    private final AuthorRedisRepository authorRedisRepository;
    private final JsonMapper jsonMapper;
    private final DeletionMarkerHandler deletionMarkerHandler;
    private final AuthorQueryService authorQueryService;

    public AuthorCommandService(
            KafkaTemplate<String, Object> kafkaTemplate,
            Cache<String, String> localCache,
            AuthorRedisRepository authorRedisRepository,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler,
            AuthorQueryService authorQueryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.localCache = localCache;
        this.authorRedisRepository = authorRedisRepository;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.authorQueryService = authorQueryService;
    }

    public CompletableFuture<AuthorCommandResult> createAuthor(CreateAuthorCommand cmd) {
        String aggregateKey = cmd.email().toLowerCase(Locale.ROOT);

        // Validate author doesn't already exist
        boolean exists = false;
        try {
            exists = authorQueryService.exists(aggregateKey);
        } catch (Exception e) {
            // Kafka streams state stores might not be initialized until the first event is published.
            // If the query service throws an exception (e.g., InvalidStateStoreException),
            // we log it and cautiously proceed with creation to avoid a bootstrap deadlock.
            log.warn(
                    "Could not verify if author exists (query service unavailable). Proceeding with creation optimistically.",
                    e);
        }

        if (exists) {
            throw new IllegalArgumentException("Author already exists with email: " + cmd.email());
        }

        // Publish domain event directly to Kafka
        AuthorCreatedEvent event = new AuthorCreatedEvent(
                aggregateKey, cmd.firstName(), cmd.middleName(), cmd.lastName(), cmd.mobile(), cmd.createdAt());
        // Build result
        AuthorCommandResult result = new AuthorCommandResult(
                aggregateKey, cmd.firstName(), cmd.middleName(), cmd.lastName(), cmd.mobile(), cmd.createdAt(), null);

        return kafkaTemplate
                .send("authors-aggregates", aggregateKey, event)
                .handleAsync(
                        (res, err) -> {
                            if (err != null) {
                                log.error("Failed to publish create author event for email: {}", cmd.email(), err);
                                throw new RuntimeException("Failed to publish create author event", err);
                            } else {
                                log.info("Successfully published create author event for email: {}", aggregateKey);
                                // Eager cache update
                                updateCaches(aggregateKey, result);
                                log.info("Author created successfully: {}", aggregateKey);
                                return result;
                            }
                        },
                        VIRTUAL_EXECUTOR);
    }

    public CompletableFuture<AuthorCommandResult> updateAuthor(UpdateAuthorCommand cmd) {
        String aggregateKey = cmd.email().toLowerCase(Locale.ROOT);

        AuthorQuery authorQuery = new AuthorQuery(aggregateKey);

        AuthorProjection author = authorQueryService.getAuthor(authorQuery);

        // Validate author exists
        if (author == null) {
            throw new ResourceNotFoundException("Author not found with id: " + cmd.email());
        }

        // Publish domain event
        AuthorUpdatedEvent event = new AuthorUpdatedEvent(
                aggregateKey,
                cmd.firstName(),
                cmd.middleName(),
                cmd.lastName(),
                cmd.mobile(),
                author.createdAt(),
                cmd.modifiedAt());
        // Build result
        AuthorCommandResult result = new AuthorCommandResult(
                aggregateKey,
                cmd.firstName(),
                cmd.middleName(),
                cmd.lastName(),
                cmd.mobile(),
                author.createdAt(),
                cmd.modifiedAt());

        return kafkaTemplate
                .send("authors-aggregates", aggregateKey, event)
                .handleAsync(
                        (res, err) -> {
                            if (err != null) {
                                log.error("Failed to publish update author event for email: {}", cmd.email(), err);
                                throw new RuntimeException("Failed to publish update author event", err);
                            } else {
                                log.info("Successfully published update author event for email: {}", aggregateKey);
                                // Eager cache update
                                updateCaches(aggregateKey, result);
                                log.info("Author updated successfully: {}", aggregateKey);
                                return result;
                            }
                        },
                        VIRTUAL_EXECUTOR);
    }

    public CompletableFuture<Void> deleteAuthor(String email) {
        String aggregateKey = email.toLowerCase(Locale.ROOT);
        log.info("Deleting author with email: {}", aggregateKey);

        // 1. Publish tombstone event
        return kafkaTemplate
                .send("authors-aggregates", aggregateKey, new AuthorDeletedEvent(aggregateKey))
                .handleAsync((res, err) -> {
                    if (err != null) {
                        log.error("Failed to publish delete author event for email: {}", aggregateKey, err);
                        throw new RuntimeException("Failed to publish delete author event", err);
                    } else {
                        log.info("Successfully published delete author event for email: {}", aggregateKey);
                        // 2. Invalidate local cache
                        localCache.invalidate(aggregateKey);

                        // 3. Remove from Redis
                        try {
                            authorRedisRepository.deleteById(aggregateKey);
                        } catch (Exception e) {
                            log.warn("Failed to delete Redis entry for email: {}", aggregateKey, e);
                        }

                        // 4. Mark deleted in Redis with TTL (prevents batch re-insertion)
                        deletionMarkerHandler.markDeleted(DeletionMarkerHandler.AUTHOR, aggregateKey);

                        log.info("Author deleted successfully: {}", aggregateKey);
                        return null;
                    }
                });
    }

    private void updateCaches(String aggregateKey, AuthorCommandResult result) {

        // Update local cache
        try {
            String json = jsonMapper.writeValueAsString(result);
            localCache.put(aggregateKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for email: {}", aggregateKey, e);
        }

        // Update Redis
        try {
            AuthorRedis authorRedis = new AuthorRedis()
                    .setEmail(aggregateKey)
                    .setFirstName(result.firstName())
                    .setMiddleName(result.middleName())
                    .setLastName(result.lastName())
                    .setMobile(result.mobile());
            authorRedis.setCreatedAt(result.createdAt());
            authorRedis.setModifiedAt(result.modifiedAt());
            authorRedisRepository.save(authorRedis);
        } catch (Exception e) {
            log.warn("Failed to update Redis for email: {}", aggregateKey, e);
        }
    }
}
