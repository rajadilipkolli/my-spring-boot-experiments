package com.example.highrps.batchprocessor;

import java.util.List;

/**
 * Strategy interface for processing batches of entity-specific payloads.
 * Implementations handle entity-specific logic for persistence and deletion.
 */
public interface EntityBatchProcessor {

    /**
     * Returns the entity type this processor handles (e.g., "post", "author", "post-comment").
     */
    String getEntityType();

    /**
     * Process a batch of upsert payloads for this entity type.
     * @param payloads JSON strings representing entity data to persist
     */
    void processUpserts(List<String> payloads);

    /**
     * Process a batch of delete operations for this entity type.
     * @param keys Keys identifying entities to delete (e.g., title, email)
     */
    void processDeletes(List<String> keys);

    /**
     * Extract the entity key from a JSON payload (e.g., title for posts, email for authors).
     * @param payload JSON string
     * @return The entity key
     */
    String extractKey(String payload);
}
