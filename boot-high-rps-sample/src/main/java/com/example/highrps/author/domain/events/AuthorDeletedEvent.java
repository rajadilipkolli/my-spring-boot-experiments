package com.example.highrps.author.domain.events;

import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tombstone event published when an author is deleted.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorDeletedEvent(String email) implements DomainEvent {}
