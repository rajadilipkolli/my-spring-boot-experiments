package com.example.highrps.postcomment.domain.events;

import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

/**
 * Domain event published when a new post comment is created.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostCommentCreatedEvent(
        Long commentId,
        Long postId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt)
        implements DomainEvent {}
