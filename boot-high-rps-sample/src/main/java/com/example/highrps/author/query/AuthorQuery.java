package com.example.highrps.author.query;

/**
 * Query to retrieve an author by email.
 */
public record AuthorQuery(String email) {
    public AuthorQuery {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }
}
