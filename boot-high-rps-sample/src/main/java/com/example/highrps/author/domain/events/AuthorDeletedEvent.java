package com.example.highrps.author.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Tombstone event published when an author is deleted.
 */
@Externalized("authors-aggregates::#{email}")
public record AuthorDeletedEvent(String email) {}
