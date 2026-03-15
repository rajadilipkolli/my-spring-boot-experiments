package com.example.highrps.author.command;

/**
 * Command to update an existing author.
 */
public record UpdateAuthorCommand(String email, String firstName, String lastName, Long mobile) {
    public UpdateAuthorCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }
}
