package com.example.jooq.spring.web.exception;

public class EntityNotFoundException extends RuntimeException {

    /** Creates a missing-entity error whose message is used as the HTTP 404 problem detail. */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
