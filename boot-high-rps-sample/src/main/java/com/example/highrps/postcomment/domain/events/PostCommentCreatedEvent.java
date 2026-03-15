package com.example.highrps.postcomment.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a new post comment is created.
 */
@Externalized("postcomments::#{id}")
public record PostCommentCreatedEvent(Long id, Long postId, String reviewText) {}
