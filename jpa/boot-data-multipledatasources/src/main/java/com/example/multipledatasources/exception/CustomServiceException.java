package com.example.multipledatasources.exception;

import org.springframework.http.HttpStatus;

public class CustomServiceException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CustomServiceException(String errorMessage, Throwable e) {
        super(errorMessage, e);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
