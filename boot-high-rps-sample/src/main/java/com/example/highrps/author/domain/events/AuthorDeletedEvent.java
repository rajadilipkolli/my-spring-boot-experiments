package com.example.highrps.author.domain.events;

/**
 * Tombstone event published when an author is deleted.
 */
public record AuthorDeletedEvent(String email) {}
