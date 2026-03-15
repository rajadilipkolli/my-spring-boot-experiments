package com.example.highrps.author.domain.events;

import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a new author is created.
 */
@Externalized("authors-aggregates::#{email}")
public record AuthorCreatedEvent(String email, String firstName, String lastName, Long mobile) {}
