package com.example.highrps.author.domain.events;

import com.example.highrps.shared.DomainEvent;

/**
 * Tombstone event published when an author is deleted.
 */
public record AuthorDeletedEvent(String email) implements DomainEvent {}
