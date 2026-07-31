package com.example.highrps.postcomment.domain.events;

import com.example.highrps.shared.DomainEvent;
import java.time.OffsetDateTime;

/**
 * Domain event published when a new post comment is created.
 */
public record PostCommentCreatedEvent(
        Long commentId,
        Long postId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt)
        implements DomainEvent {}
