package com.example.highrps.post.query;

import java.time.LocalDateTime;

/**
 * Projection for post read model.
 * This is the read-side representation optimized for queries.
 */
public record PostProjection(
        Long postId,
        String title,
        String content,
        String authorEmail,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
