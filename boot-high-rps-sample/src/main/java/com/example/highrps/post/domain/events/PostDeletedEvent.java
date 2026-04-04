package com.example.highrps.post.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Tombstone event published when a post is deleted.
 * This event is externalized to Kafka topic 'posts-aggregates' as a tombstone
 * (null value).
 */
@Externalized("posts-aggregates::#{postId}")
public record PostDeletedEvent(Long postId) {}
