package com.example.highrps.author.command;

import java.time.LocalDateTime;

/**
 * Command to update an existing author.
 */
public record UpdateAuthorCommand(
        String email, String firstName, String middleName, String lastName, Long mobile, LocalDateTime modifiedAt) {
    public UpdateAuthorCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }
}
