package com.example.highrps.postcomment.command;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Immutable result returned from post comment command operations.
 *
 * <p>Designed for high-throughput serialization/deserialization.</p>
 */
public record PostCommentCommandResult(
        long id,
        long postId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {

    /*
     * JsonMapper is thread-safe after construction/configuration.
     * Keep a single immutable instance instead of creating one per call.
     */
    private static final JsonMapper MAPPER = new JsonMapper();

    /**
     * Deserialize a JSON representation from cache.
     *
     * @param json JSON payload
     * @return deserialized result
     * @throws IllegalArgumentException if json is null
     * @throws IllegalStateException if deserialization fails
     */
    public static PostCommentCommandResult fromJson(String json) {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }

        try {
            return MAPPER.readValue(json, PostCommentCommandResult.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize PostCommentCommandResult from JSON", e);
        }
    }

    /**
     * Serialize this result to JSON for cache storage.
     *
     * @return JSON representation
     * @throws IllegalStateException if serialization fails
     */
    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize PostCommentCommandResult to JSON", e);
        }
    }
}
