package com.example.highrps.author.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when an author is updated.
 */
@Externalized("authors-aggregates::#{email}")
public record AuthorUpdatedEvent(String email, String firstName, String lastName, Long mobile) {}
