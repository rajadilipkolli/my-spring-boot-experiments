package com.example.highrps.shared;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import java.util.Locale;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base template class for consuming aggregates from Kafka and updating the Redis cache layer.
 *
 * @param <T> The payload type expected from the Kafka event
 */
public abstract class AbstractAggregatesToRedisListener<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractAggregatesToRedisListener.class);

    protected final RedisTemplate<String, String> redis;
    protected final String queueKey;
    protected final JsonMapper jsonMapper;
    protected final DeletionMarkerHandler deletionMarkerHandler;
    private final Class<T> payloadClass;

    protected AbstractAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            String queueKey,
            JsonMapper jsonMapper,
            DeletionMarkerHandler deletionMarkerHandler,
            Class<T> payloadClass) {
        this.redis = redis;
        this.queueKey = queueKey;
        this.jsonMapper = jsonMapper;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.payloadClass = payloadClass;
    }

    /**
     * Delete the entity from the specific Redis repository.
     */
    protected abstract void deleteFromRepository(String key);

    /**
     * Map the payload to the specific Redis entity and save it to the repository.
     */
    protected abstract void saveToRepository(T payload, String key);

    /**
     * Return the entity type string used for batching (e.g. "author", "post").
     */
    protected abstract String getEntityType();

    /**
     * Return the DeletionMarkerHandler marker type (e.g. DeletionMarkerHandler.AUTHOR).
     */
    protected abstract String getMarkerType();

    /**
     * Return the specific field name used in the tombstone/deleted domain event.
     * (e.g. "email", "postId", "id")
     */
    protected abstract String getDeletionIdentifierField();

    /**
     * Check if the parsed node represents a deletion event.
     */
    protected boolean isDeletionEvent(JsonNode node) {
        return node.has(getDeletionIdentifierField()) && node.size() == 1;
    }

    /**
     * Delete logic for a deletion event. Default implementation assumes a single key.
     */
    protected void processDeletionEvent(JsonNode node, String key, String topicName) {
        log.info("Identified deletion event for cacheKey: {} on topic {}", key, topicName);
        handleDeletion(key, key);
    }

    /**
     * Prepares the payload string to enqueue for async DB writes.
     */
    protected String prepareEnqueuePayload(JsonNode node, T payload) throws Exception {
        String jsonString = jsonMapper.writeValueAsString(node);
        if (jsonString.startsWith("{")) {
            jsonString = "{\"__entity\":\"" + getEntityType() + "\"," + jsonString.substring(1);
        }
        return jsonString;
    }

    /**
     * Primary template method to process aggregate messages.
     */
    protected void processAggregate(ConsumerRecord<String, byte[]> record, String topicName) {
        try {
            String key = record.key();
            if (key == null) {
                log.warn("Received message with null key on {}, ignoring.", topicName);
                return;
            }
            byte[] bytes = record.value();
            log.debug(
                    "Received {} record: partition={}, offset={}, cacheKey={}, valueIsNull={}",
                    topicName,
                    record.partition(),
                    record.offset(),
                    key,
                    bytes == null);

            // If payload is null -> tombstone: remove Redis key and enqueue delete marker
            if (bytes == null) {
                handleTombstone(key, topicName);
                return;
            }

            JsonNode node = jsonMapper.readTree(bytes);

            // Check for explicit deletion event
            if (isDeletionEvent(node)) {
                processDeletionEvent(node, key, topicName);
                return;
            }

            T payload = jsonMapper.treeToValue(node, payloadClass);

            // Update Redis Repository (Cache)
            try {
                String cacheKey = getCacheKey(payload, key);
                if (deletionMarkerHandler.isDeleted(getMarkerType(), cacheKey)) {
                    log.info("Skipping Redis update for {} {} as it is marked as deleted", getEntityType(), cacheKey);
                } else {
                    saveToRepository(payload, key);
                }
            } catch (Exception e) {
                log.warn("Failed to update redis repository for key: {}", key, e);
            }

            // Enqueue payload for asynchronous DB writes
            try {
                String jsonString = prepareEnqueuePayload(node, payload);
                redis.opsForList().leftPush(queueKey, jsonString);
            } catch (Exception e) {
                log.error("Failed to enqueue payload for DB write, cacheKey: {}, may lose durability", key, e);
                throw new IllegalStateException("Failed to enqueue payload for DB write, key=" + key, e);
            }
        } catch (Exception e) {
            log.error("Unhandled exception in handleAggregate for topic {}", topicName, e);
            throw new RuntimeException(e);
        }
    }

    protected void handleTombstone(String key, String topicName) {
        handleDeletion(key, key);
    }

    /**
     * Get the cache key for checking the deletion marker.
     */
    protected String getCacheKey(T payload, String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    protected void handleDeletion(String repositoryKey, String markerKey) {
        try {
            deleteFromRepository(repositoryKey);
        } catch (Exception e) {
            log.warn("Failed to delete repository entry for deletion of key: {}", repositoryKey, e);
        }
        try {
            String cacheKey = markerKey.toLowerCase(Locale.ROOT);
            deletionMarkerHandler.markDeleted(getMarkerType(), cacheKey);
            String tombstoneJson = jsonMapper.writeValueAsString(Map.of(
                    getDeletionIdentifierField(), repositoryKey, "__deleted", true, "__entity", getEntityType()));
            redis.opsForList().leftPush(queueKey, tombstoneJson);
        } catch (Exception e) {
            log.error("Failed to enqueue tombstone marker for key: {}, may lose durability", markerKey, e);
            throw new IllegalStateException("Failed to enqueue tombstone marker for key=" + markerKey, e);
        }
    }

    /**
     * Generic Dead Letter Queue handler.
     */
    protected void handleDlt(ConsumerRecord<String, byte[]> record, String topic) {
        log.error("Received dead-letter message from topic {}", topic);
        String dlqKey = "dlq:" + topic;
        try {
            String payload = record.value() != null ? new String(record.value()) : "null";
            redis.opsForList().leftPush(dlqKey, payload);
        } catch (Exception e) {
            log.warn("Failed to push to DLQ: {}", dlqKey, e);
        }
    }
}
