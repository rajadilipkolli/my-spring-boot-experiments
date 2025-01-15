package com.example.multipledatasources.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for handling service-layer errors with associated HTTP status codes.
 * This exception is thrown when service operations fail and need to communicate
 * both an error message and an appropriate HTTP status code to the client.
 */
public class CustomServiceException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomServiceException(String errorMessage, Throwable e, HttpStatus httpStatus) {
        super(errorMessage, e);
        this.httpStatus = httpStatus;
    }

    public CustomServiceException(String errorMessage, Throwable e) {
        this(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
