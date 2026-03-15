package com.example.highrps.author.command;

import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.example.highrps.author.domain.events.AuthorDeletedEvent;
import com.example.highrps.author.domain.events.AuthorUpdatedEvent;
import com.example.highrps.entities.AuthorRedis;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, String> redis;
    private final AuthorRedisRepository authorRedisRepository;
    private final JsonMapper jsonMapper;

    public AuthorCommandService(
            ApplicationEventPublisher events,
            Cache<String, String> localCache,
            RedisTemplate<String, String> redis,
            AuthorRedisRepository authorRedisRepository,
            JsonMapper jsonMapper) {
        this.events = events;
        this.localCache = localCache;
        this.redis = redis;
        this.authorRedisRepository = authorRedisRepository;
        this.jsonMapper = jsonMapper;
    }

    public AuthorCommandResult createAuthor(CreateAuthorCommand cmd) {
        log.info("Creating author with email: {}", cmd.email());

        // Publish domain event (transactional)
        AuthorCreatedEvent event = new AuthorCreatedEvent(cmd.email(), cmd.firstName(), cmd.lastName(), cmd.mobile());
        events.publishEvent(event);

        // Build result
        AuthorCommandResult result =
                new AuthorCommandResult(cmd.email(), cmd.firstName(), cmd.lastName(), cmd.mobile());

        // Eager cache update
        updateCaches(cmd.email(), result);

        log.info("Author created successfully: {}", cmd.email());
        return result;
    }

    public AuthorCommandResult updateAuthor(UpdateAuthorCommand cmd) {
        log.info("Updating author with email: {}", cmd.email());

        // Publish domain event
        AuthorUpdatedEvent event = new AuthorUpdatedEvent(cmd.email(), cmd.firstName(), cmd.lastName(), cmd.mobile());
        events.publishEvent(event);

        // Build result
        AuthorCommandResult result =
                new AuthorCommandResult(cmd.email(), cmd.firstName(), cmd.lastName(), cmd.mobile());

        // Eager cache update
        updateCaches(cmd.email(), result);

        log.info("Author updated successfully: {}", cmd.email());
        return result;
    }

    public void deleteAuthor(String email) {
        log.info("Deleting author with email: {}", email);

        // 1. Publish tombstone event
        events.publishEvent(new AuthorDeletedEvent(email));

        // 2. Invalidate local cache
        String cacheKey = email.toLowerCase();
        localCache.invalidate(cacheKey);

        // 3. Remove from Redis
        try {
            authorRedisRepository.deleteById(email);
        } catch (Exception e) {
            log.warn("Failed to delete Redis entry for email: {}", email, e);
        }

        // 4. Mark deleted in Redis with TTL
        try {
            redis.opsForValue().set("deleted:authors:" + email, "1", Duration.ofSeconds(60));
        } catch (Exception e) {
            log.warn("Failed to mark author as deleted in Redis: {}", email, e);
        }

        log.info("Author deleted successfully: {}", email);
    }

    private void updateCaches(String email, AuthorCommandResult result) {
        String cacheKey = email.toLowerCase();

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
                    .setEmail(result.email())
                    .setFirstName(result.firstName())
                    .setLastName(result.lastName())
                    .setMobile(result.mobile());

            authorRedisRepository.save(authorRedis);
        } catch (Exception e) {
            log.warn("Failed to update Redis for email: {}", email, e);
        }
    }
}
