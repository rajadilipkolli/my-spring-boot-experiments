package com.example.highrps.post.domain.events;

import com.example.highrps.shared.DomainEvent;

/**
 * Tombstone event published when a post is deleted.
 * This event is externalized to Kafka topic 'posts-aggregates' as a tombstone
 * (null value).
 */
public record PostDeletedEvent(Long postId) implements DomainEvent {}
