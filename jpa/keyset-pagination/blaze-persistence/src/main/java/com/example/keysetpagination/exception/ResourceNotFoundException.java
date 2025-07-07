package com.example.keysetpagination.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    private final HttpStatus httpStatus = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
