package com.example.highrps.author.domain.events;

import java.time.LocalDateTime;

/**
 * Domain event published when a new author is created.
 */
public record AuthorCreatedEvent(
        String email, String firstName, String middleName, String lastName, Long mobile, LocalDateTime createdAt) {}
