package com.example.highrps.postcomment.domain.events;

import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Domain event published when a post comment is updated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostCommentUpdatedEvent(
        Long commentId,
        Long postId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt)
        implements DomainEvent {}
