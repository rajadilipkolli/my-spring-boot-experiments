package com.example.highrps.shared;

public class ResourceConflictException extends RuntimeException {
    /**
     * Creates a conflict exception with a client-facing message.
     *
     * @param message the conflict description
     */
    public ResourceConflictException(String message) {
        super(message);
    }
}
