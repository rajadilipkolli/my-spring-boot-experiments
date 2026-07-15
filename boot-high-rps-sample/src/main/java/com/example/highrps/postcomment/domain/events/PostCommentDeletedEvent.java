package com.example.highrps.postcomment.domain.events;

/**
 * Domain event published when a post comment is deleted.
 */
public record PostCommentDeletedEvent(Long commentId, Long postId) {}
