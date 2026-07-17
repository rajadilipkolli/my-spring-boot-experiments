package com.example.highrps.author.command;

import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.example.highrps.author.domain.events.AuthorDeletedEvent;
import com.example.highrps.author.domain.events.AuthorUpdatedEvent;
import com.example.highrps.author.query.AuthorProjection;
import com.example.highrps.author.query.AuthorQuery;
import com.example.highrps.author.query.AuthorQueryService;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.shared.AbstractCommandService;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Command service for Author aggregate.
 * Handles all write operations and publishes domain events.
 */
@Service
public class AuthorCommandService extends AbstractCommandService {

    private static final Logger log = LoggerFactory.getLogger(AuthorCommandService.class);

    private final Cache<String, String> localCache;
    private final JsonMapper jsonMapper;
    private final DeletionMarkerHandler deletionMarkerHandler;
    private final AuthorQueryService authorQueryService;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthorCommandService(
            KafkaTemplate<String, Object> kafkaTemplate,
            Cache<String, String> localCache,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler,
            AuthorQueryService authorQueryService,
            RedisTemplate<String, String> redisTemplate) {
        super(kafkaTemplate);
        this.localCache = localCache;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.authorQueryService = authorQueryService;
        this.redisTemplate = redisTemplate;
    }

    public CompletableFuture<AuthorCommandResult> createAuthor(CreateAuthorCommand cmd) {
        String aggregateKey = cmd.email().toLowerCase(Locale.ROOT);

        String reservationKey = "reservation:author:" + aggregateKey;
        // Acquire an atomic distributed reservation to prevent concurrent duplicate creations
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(reservationKey, "1", Duration.ofMinutes(5));

        if (Boolean.FALSE.equals(acquired)) {
            throw new IllegalArgumentException("Author already exists with email: " + cmd.email());
        }

        // Validate author doesn't already exist in the read model as a fallback
        boolean exists = false;
        try {
            exists = authorQueryService.exists(aggregateKey);
        } catch (Exception e) {
            log.warn(
                    "Could not verify if author exists (query service unavailable). Relying on distributed reservation.",
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

        return executeCommand(
                        "authors-aggregates",
                        aggregateKey,
                        aggregateKey,
                        event,
                        result,
                        () -> updateCaches(aggregateKey, result),
                        "create author",
                        "Author")
                .whenComplete((res, err) -> {
                    if (err != null) {
                        try {
                            redisTemplate.delete(reservationKey);
                        } catch (Exception e) {
                            log.warn(
                                    "Failed to clean up reservation key after creation failure: {}", reservationKey, e);
                        }
                    }
                });
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

        return executeCommand(
                "authors-aggregates",
                aggregateKey,
                aggregateKey,
                event,
                result,
                () -> updateCaches(aggregateKey, result),
                "update author",
                "Author");
    }

    public CompletableFuture<Void> deleteAuthor(String email) {
        String aggregateKey = email.toLowerCase(Locale.ROOT);
        log.info("Deleting author with email: {}", aggregateKey);

        // 1. Publish tombstone event
        return executeCommand(
                "authors-aggregates",
                aggregateKey,
                aggregateKey,
                new AuthorDeletedEvent(aggregateKey),
                null, // Void result
                () -> {
                    // 2. Invalidate local cache
                    localCache.invalidate(aggregateKey);
                    // 3. Mark deleted in Redis with TTL (prevents batch re-insertion)
                    deletionMarkerHandler.markDeleted(DeletionMarkerHandler.AUTHOR, aggregateKey);
                },
                "delete author",
                "Author");
    }

    private void updateCaches(String aggregateKey, AuthorCommandResult result) {

        // Update local cache
        try {
            String json = jsonMapper.writeValueAsString(result);
            localCache.put(aggregateKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for email: {}", aggregateKey, e);
        }
    }
}
