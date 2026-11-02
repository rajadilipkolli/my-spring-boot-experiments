package com.example.mongoes.web.exception;

import org.springframework.http.HttpStatus;

public class RestaurantNotFoundException extends RuntimeException {

    private final HttpStatus httpStatus;

    public RestaurantNotFoundException(String message) {
        super(message);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
