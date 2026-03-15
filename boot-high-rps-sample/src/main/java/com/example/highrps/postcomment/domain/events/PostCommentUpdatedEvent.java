package com.example.highrps.postcomment.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a post comment is updated.
 */
@Externalized("postcomments::#{id}")
public record PostCommentUpdatedEvent(Long id, Long postId, String reviewText) {}
