package com.example.highrps.author.domain.events;

import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Domain event published when a new author is created.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorCreatedEvent(
        String email, String firstName, String middleName, String lastName, Long mobile, LocalDateTime createdAt)
        implements DomainEvent {}
