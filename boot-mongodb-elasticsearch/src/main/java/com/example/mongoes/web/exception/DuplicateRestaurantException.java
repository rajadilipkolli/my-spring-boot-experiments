package com.example.mongoes.web.exception;

import org.springframework.http.HttpStatus;

public class DuplicateRestaurantException extends RuntimeException {

    private final HttpStatus httpStatus;

    public DuplicateRestaurantException(String message) {
        super(message);
        this.httpStatus = HttpStatus.CONFLICT;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
