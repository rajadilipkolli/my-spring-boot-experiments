package com.example.highrps.author.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Projection for author read model.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorProjection(
        String email,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        LocalDateTime registeredAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
