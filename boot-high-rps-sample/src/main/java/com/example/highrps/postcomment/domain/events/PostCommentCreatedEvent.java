package com.example.highrps.postcomment.domain.events;

import java.time.OffsetDateTime;
import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a new post comment is created.
 */
@Externalized("post-comments-aggregates::#{commentId}")
public record PostCommentCreatedEvent(
        Long commentId,
        Long postId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt) {}
