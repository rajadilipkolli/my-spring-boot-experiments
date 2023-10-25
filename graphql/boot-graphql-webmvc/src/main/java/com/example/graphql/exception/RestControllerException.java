package com.example.graphql.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RestControllerException extends RuntimeException {

    final HttpStatus httpStatus;

    public RestControllerException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.httpStatus = httpStatus;
    }
}
