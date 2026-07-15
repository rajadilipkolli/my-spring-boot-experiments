package com.example.highrps.postcomment.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a post comment is deleted.
 */
@Externalized("post-comments-aggregates::#{commentId}")
public record PostCommentDeletedEvent(Long commentId, Long postId) {}
