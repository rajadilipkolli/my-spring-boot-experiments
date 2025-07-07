package com.example.graphql.exception;

import org.springframework.http.HttpStatus;

public class RestControllerException extends RuntimeException {

    final HttpStatus httpStatus;

    public RestControllerException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
