package com.example.highrps.author.command;

import java.time.LocalDateTime;

/**
 * Result returned from author command operations.
 */
public record AuthorCommandResult(
        String email,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
