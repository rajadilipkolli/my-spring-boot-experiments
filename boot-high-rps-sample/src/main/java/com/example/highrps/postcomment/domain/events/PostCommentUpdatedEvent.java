package com.example.highrps.postcomment.domain.events;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Domain event published when a post comment is updated.
 */
public record PostCommentUpdatedEvent(
        Long commentId,
        Long postId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
