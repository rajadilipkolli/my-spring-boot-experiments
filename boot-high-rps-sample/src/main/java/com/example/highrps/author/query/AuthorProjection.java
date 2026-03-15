package com.example.highrps.author.query;

/**
 * Projection for author read model.
 */
public record AuthorProjection(String email, String firstName, String lastName, Long mobile) {}
