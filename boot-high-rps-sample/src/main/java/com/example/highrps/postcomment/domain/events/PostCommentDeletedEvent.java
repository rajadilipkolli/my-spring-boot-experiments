package com.example.highrps.postcomment.domain.events;

import com.example.highrps.shared.DomainEvent;

/**
 * Domain event published when a post comment is deleted.
 */
public record PostCommentDeletedEvent(Long commentId, Long postId) implements DomainEvent {}
