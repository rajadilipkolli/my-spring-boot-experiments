package com.example.highrps.author.domain.events;

import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Domain event published when an author is updated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorUpdatedEvent(
        String email,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt)
        implements DomainEvent {}
