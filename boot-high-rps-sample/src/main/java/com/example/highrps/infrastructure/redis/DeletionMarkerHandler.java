package com.example.highrps.infrastructure.redis;

import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Centrally manages Redis deletion markers to prevent eventual consistency issues
 * between eager caches and async batch persistence.
 */
@Component
public class DeletionMarkerHandler {

    private static final Logger log = LoggerFactory.getLogger(DeletionMarkerHandler.class);
    private static final String MARKER_PREFIX = "deleted:";
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(60);

    private final RedisTemplate<String, String> redis;

    public DeletionMarkerHandler(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    /**
     * Marks an entity as deleted in Redis for a short duration.
     * Use this when a deletion is initiated to prevent old upserts from resurrecting the entity.
     */
    public void markDeleted(String entityType, String key) {
        String markerKey = getMarkerKey(entityType, key);
        try {
            redis.opsForValue().set(markerKey, "1", DEFAULT_TTL);
            log.debug("Marked entity as deleted in Redis: {}", markerKey);
        } catch (Exception e) {
            log.warn("Failed to mark entity as deleted in Redis: {}", markerKey, e);
        }
    }

    /**
     * Checks if an entity is currently marked as deleted in Redis.
     */
    public boolean isDeleted(String entityType, String key) {
        String markerKey = getMarkerKey(entityType, key);
        try {
            return Boolean.TRUE.equals(redis.hasKey(markerKey));
        } catch (Exception e) {
            log.warn("Failed to check deletion marker in Redis: {}", markerKey, e);
            return false;
        }
    }

    private String getMarkerKey(String entityType, String key) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Objects.requireNonNull(key, "key must not be null");
        return MARKER_PREFIX + entityType + ":" + key;
    }
}
