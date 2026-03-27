package com.example.highrps.author.domain.events;

import java.time.LocalDateTime;
import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a new author is created.
 */
@Externalized("authors-aggregates::#{email}")
public record AuthorCreatedEvent(
        String email,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        LocalDateTime registeredAt,
        LocalDateTime createdAt) {}