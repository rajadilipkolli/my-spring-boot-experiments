package com.example.highrps.author.query;

import java.time.LocalDateTime;

/**
 * Projection for author read model.
 */
public record AuthorProjection(
        String email,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        LocalDateTime registeredAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
