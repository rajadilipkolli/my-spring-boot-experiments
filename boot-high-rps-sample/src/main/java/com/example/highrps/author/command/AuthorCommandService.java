package com.example.highrps.author.command;

import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.example.highrps.author.domain.events.AuthorDeletedEvent;
import com.example.highrps.author.domain.events.AuthorUpdatedEvent;
import com.example.highrps.author.query.AuthorProjection;
import com.example.highrps.author.query.AuthorQuery;
import com.example.highrps.author.query.AuthorQueryService;
import com.example.highrps.entities.AuthorRedis;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import com.example.highrps.shared.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Command service for Author aggregate.
 * Handles all write operations and publishes domain events.
 */
@Service
@Transactional
public class AuthorCommandService {

    private static final Logger log = LoggerFactory.getLogger(AuthorCommandService.class);

    private final ApplicationEventPublisher events;
    private final Cache<String, String> localCache;
    private final AuthorRedisRepository authorRedisRepository;
    private final JsonMapper jsonMapper;
    private final DeletionMarkerHandler deletionMarkerHandler;
    private final AuthorQueryService authorQueryService;

    public AuthorCommandService(
            ApplicationEventPublisher events,
            Cache<String, String> localCache,
            AuthorRedisRepository authorRedisRepository,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler,
            AuthorQueryService authorQueryService) {
        this.events = events;
        this.localCache = localCache;
        this.authorRedisRepository = authorRedisRepository;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.authorQueryService = authorQueryService;
    }

    public AuthorCommandResult createAuthor(CreateAuthorCommand cmd) {
        // Publish domain event (transactional)
        AuthorCreatedEvent event = new AuthorCreatedEvent(
                cmd.email(), cmd.firstName(), cmd.middleName(), cmd.lastName(), cmd.mobile(), cmd.createdAt());
        events.publishEvent(event);

        // Build result
        AuthorCommandResult result = new AuthorCommandResult(
                cmd.email(), cmd.firstName(), cmd.middleName(), cmd.lastName(), cmd.mobile(), cmd.createdAt(), null);

        // Eager cache update
        updateCaches(cmd.email(), result);

        log.info("Author created successfully: {}", cmd.email());
        return result;
    }

    public AuthorCommandResult updateAuthor(UpdateAuthorCommand cmd) {

        AuthorQuery authorQuery = new AuthorQuery(cmd.email());

        AuthorProjection author = authorQueryService.getAuthor(authorQuery);

        // Validate author exists
        if (author == null) {
            throw new ResourceNotFoundException("Author not found with id: " + cmd.email());
        }

        // Publish domain event
        AuthorUpdatedEvent event = new AuthorUpdatedEvent(
                cmd.email(),
                cmd.firstName(),
                cmd.middleName(),
                cmd.lastName(),
                cmd.mobile(),
                author.createdAt(),
                cmd.modifiedAt());
        events.publishEvent(event);

        // Build result
        AuthorCommandResult result = new AuthorCommandResult(
                cmd.email(),
                cmd.firstName(),
                cmd.middleName(),
                cmd.lastName(),
                cmd.mobile(),
                author.createdAt(),
                cmd.modifiedAt());

        // Eager cache update
        updateCaches(cmd.email(), result);

        log.info("Author updated successfully: {}", cmd.email());
        return result;
    }

    public void deleteAuthor(String email) {
        log.info("Deleting author with email: {}", email);

        String cacheKey = email.toLowerCase(Locale.ROOT);
        // 1. Publish tombstone event
        events.publishEvent(new AuthorDeletedEvent(cacheKey));

        // 2. Invalidate local cache
        localCache.invalidate(cacheKey);

        // 3. Remove from Redis
        try {
            authorRedisRepository.deleteById(cacheKey);
        } catch (Exception e) {
            log.warn("Failed to delete Redis entry for email: {}", cacheKey, e);
        }

        // 4. Mark deleted in Redis with TTL
        deletionMarkerHandler.markDeleted(DeletionMarkerHandler.AUTHOR, cacheKey);

        log.info("Author deleted successfully: {}", email);
    }

    private void updateCaches(String email, AuthorCommandResult result) {
        String cacheKey = email.toLowerCase(Locale.ROOT);

        // Update local cache
        try {
            String json = jsonMapper.writeValueAsString(result);
            localCache.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to update local cache for email: {}", email, e);
        }

        // Update Redis
        try {
            AuthorRedis authorRedis = new AuthorRedis()
                    .setEmail(result.email().toLowerCase(Locale.ROOT))
                    .setFirstName(result.firstName())
                    .setMiddleName(result.middleName())
                    .setLastName(result.lastName())
                    .setMobile(result.mobile());
            authorRedis.setCreatedAt(result.createdAt());
            authorRedis.setModifiedAt(result.modifiedAt());
            authorRedisRepository.save(authorRedis);
        } catch (Exception e) {
            log.warn("Failed to update Redis for email: {}", email, e);
        }
    }
}
