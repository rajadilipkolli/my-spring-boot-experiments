package com.example.highrps.postcomment.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Tombstone event published when a post comment is deleted.
 */
@Externalized("postcomments::#{id}")
public record PostCommentDeletedEvent(Long id) {}
