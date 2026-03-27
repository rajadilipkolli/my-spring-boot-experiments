package com.example.highrps.author.domain.events;

import java.time.LocalDateTime;
import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when an author is updated.
 */
@Externalized("authors-aggregates::#{email}")
public record AuthorUpdatedEvent(
        String email,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
