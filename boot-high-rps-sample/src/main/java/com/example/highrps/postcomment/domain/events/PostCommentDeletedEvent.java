package com.example.highrps.postcomment.domain.events;

import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Domain event published when a post comment is deleted.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostCommentDeletedEvent(Long commentId, Long postId) implements DomainEvent {}
