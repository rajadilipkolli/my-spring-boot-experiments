package com.example.highrps.author.domain.events;

import java.time.LocalDateTime;

/**
 * Domain event published when an author is updated.
 */
public record AuthorUpdatedEvent(
        String email,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
