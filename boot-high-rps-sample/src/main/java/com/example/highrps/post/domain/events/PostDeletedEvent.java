package com.example.highrps.post.domain.events;

import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tombstone event published when a post is deleted.
 * This event is externalized to Kafka topic 'posts-aggregates' as a tombstone
 * (null value).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostDeletedEvent(Long postId) implements DomainEvent {}
