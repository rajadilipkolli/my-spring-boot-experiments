package com.example.highrps.infrastructure.kafka.batch;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Generic batch processor that handles asynchronous persistence of entities from Redis queue.
 * Uses strategy pattern to delegate entity-specific operations to EntityBatchProcessor implementations.
 * Supports multiple entity types (posts, authors, etc.) with automatic routing based on entity metadata.
 */
@Component
public class ScheduledBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduledBatchProcessor.class);

    private final RedisTemplate<String, String> redis;
    private final JsonMapper jsonMapper;
    private final Map<String, EntityBatchProcessor> processorsByEntityType;
    private final DeletionMarkerHandler deletionMarkerHandler;

    private final String queueKey;
    private final int batchSize;

    public ScheduledBatchProcessor(
            RedisTemplate<String, String> redis,
            JsonMapper jsonMapper,
            List<EntityBatchProcessor> processors,
            @Value("${app.batch.queue-key}") String queueKey,
            @Value("${app.batch.size}") int batchSize,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
        this.queueKey = queueKey;
        this.batchSize = batchSize;
        this.deletionMarkerHandler = deletionMarkerHandler;

        // Build registry of processors by entity type
        this.processorsByEntityType =
                processors.stream().collect(Collectors.toMap(EntityBatchProcessor::getEntityType, p -> p));

        log.info(
                "Initialized ScheduledBatchProcessor with {} entity processors: {}",
                processorsByEntityType.size(),
                processorsByEntityType.keySet());
    }

    @Scheduled(fixedDelayString = "${app.batch.delay-ms}")
    public void processBatch() {
        List<String> items = redis.opsForList().rightPop(queueKey, batchSize);
        if (items == null || items.isEmpty()) {
            return;
        }

        // NOTE: do not bind a single entity-type deleted set up front. We will check the per-entity
        // deleted set (e.g. deleted:posts, deleted:authors) when the entityType is known to avoid
        // re-inserting entities that were recently deleted.

        // Group items by entity type, then deduplicate by key within each entity type
        Map<String, Map<String, PayloadOrTombstone>> groupedByEntityType = new HashMap<>();

        for (String item : items) {
            if (item == null) continue;

            try {
                var node = jsonMapper.readTree(item);
                String entityType = node.has("__entity") ? node.get("__entity").asString() : null;
                boolean isDeleted =
                        node.has("__deleted") && node.get("__deleted").asBoolean(false);

                EntityBatchProcessor processor = processorsByEntityType.get(entityType);
                if (processor == null) {
                    log.warn("No processor found for entity type: {}", entityType);
                    continue;
                }

                String key = processor.extractKey(item);
                if (key == null) {
                    log.warn("Failed to extract key from payload for entity type: {}", entityType);
                    continue;
                }

                // If this entity was deleted recently, skip any queued upsert that could resurrect it.
                // Tombstones still flow through normally.
                if (entityType != null && !isDeleted) {
                    if (deletionMarkerHandler.isDeleted(entityType, key)) {
                        log.debug(
                                "Skipping queued upsert because it is marked deleted: entity={}, key={}",
                                entityType,
                                key);
                        continue;
                    }
                }

                // Place into per-entity map, with tombstone taking precedence over payloads.
                var perKey = groupedByEntityType.computeIfAbsent(entityType, k -> new HashMap<>());
                PayloadOrTombstone existing = perKey.get(key);
                if (isDeleted) {
                    // Always prefer tombstone for this key and store the original raw tombstone payload so it can
                    // be re-queued if downstream processing fails.
                    log.debug("Grouping: tombstone for entity={}, key={}", entityType, key);
                    perKey.put(key, PayloadOrTombstone.tombstone(item, key));
                } else {
                    log.debug("Grouping: upsert for entity={}, key={}", entityType, key);
                    // Only store payload if we don't already have a tombstone for this key
                    if (existing == null || !existing.isTombstone()) {
                        perKey.put(key, PayloadOrTombstone.payload(item));
                    } else {
                        log.debug(
                                "Skipping upsert because tombstone already present for entity={}, key={}",
                                entityType,
                                key);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse queued item: {}", item, e);
            }
        }

        // Process each entity type's batch
        groupedByEntityType.forEach((entityType, payloadsByKey) -> {
            EntityBatchProcessor processor = processorsByEntityType.get(entityType);
            if (processor == null) return;

            // Separate deletes and upserts
            List<String> deletes = payloadsByKey.values().stream()
                    .filter(PayloadOrTombstone::isTombstone)
                    .map(PayloadOrTombstone::key)
                    .toList();

            List<String> upserts = payloadsByKey.values().stream()
                    .filter(p -> !p.isTombstone())
                    .map(PayloadOrTombstone::payload)
                    .toList();

            try {
                log.debug(
                        "Processing batch for entity={}, deletes={}, upserts= {}",
                        entityType,
                        deletes.size(),
                        upserts.size());

                if (!upserts.isEmpty()) {
                    try {
                        processor.processUpserts(upserts);
                    } catch (Exception e) {
                        log.warn(
                                "Batch upsert failed for entity type: {}, attempting individual processing to isolate poison pills",
                                entityType);
                        processUpsertsIndividually(processor, upserts);
                    }
                }

                if (!deletes.isEmpty()) {
                    try {
                        processor.processDeletes(deletes);
                    } catch (Exception e) {
                        log.warn(
                                "Batch delete failed for entity type: {}, attempting individual processing",
                                entityType);
                        processDeletesIndividually(processor, deletes);
                    }
                }
            } catch (Exception e) {
                log.error("Unexpected error in batch processing for entity type: {}", entityType, e);
            }
        });
    }

    private void processUpsertsIndividually(EntityBatchProcessor processor, List<String> payloads) {
        String entityType = processor.getEntityType();
        String dlqKey = "dlq:batch:" + entityType;

        for (String payload : payloads) {
            try {
                processor.processUpserts(List.of(payload));
            } catch (Exception e) {
                log.error("Failed to process individual upsert for {}, moving to DLQ: {}", entityType, payload, e);
                try {
                    redis.opsForList().leftPush(dlqKey, payload);
                } catch (Exception re) {
                    log.error("CRITICAL: Failed to push poison pill to DLQ: {}", dlqKey, re);
                }
            }
        }
    }

    private void processDeletesIndividually(EntityBatchProcessor processor, List<String> keys) {
        String entityType = processor.getEntityType();
        String dlqKey = "dlq:batch:deletes:" + entityType;

        for (String key : keys) {
            try {
                processor.processDeletes(List.of(key));
            } catch (Exception e) {
                log.error("Failed to process individual delete for {} key {}, moving to DLQ", entityType, key, e);
                try {
                    redis.opsForList().leftPush(dlqKey, key);
                } catch (Exception re) {
                    log.error("CRITICAL: Failed to push delete failure to DLQ: {}", dlqKey, re);
                }
            }
        }
    }

    /**
     * Internal record to represent either a payload to persist or a tombstone (delete marker).
     */
    private record PayloadOrTombstone(String payload, String key, boolean isTombstone) {
        static PayloadOrTombstone payload(String payload) {
            return new PayloadOrTombstone(payload, null, false);
        }

        static PayloadOrTombstone tombstone(String payload, String key) {
            return new PayloadOrTombstone(payload, key, true);
        }
    }
}
