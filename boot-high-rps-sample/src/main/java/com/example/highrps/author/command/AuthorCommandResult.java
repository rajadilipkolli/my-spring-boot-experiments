package com.example.highrps.author.command;

/**
 * Result returned from author command operations.
 */
public record AuthorCommandResult(String email, String firstName, String lastName, Long mobile) {}
