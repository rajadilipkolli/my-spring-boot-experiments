package com.example.highrps.author.command;

import java.time.LocalDateTime;

/**
 * Command to create a new author.
 */
public record CreateAuthorCommand(
        String email, String firstName, String middleName, String lastName, Long mobile, LocalDateTime createdAt) {
    public CreateAuthorCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName must not be blank");
        }
    }
}
