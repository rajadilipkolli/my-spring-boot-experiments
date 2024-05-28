package com.example.jndi.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    private static final HttpStatus httpStatus = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
