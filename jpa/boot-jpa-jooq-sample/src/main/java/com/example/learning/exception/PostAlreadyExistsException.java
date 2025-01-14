package com.example.learning.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class PostAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final HttpStatus httpStatus = HttpStatus.CONFLICT;

    public PostAlreadyExistsException(String title) {
        super("Post with title : " + title + " already exists");
    }

    public HttpStatusCode getHttpStatus() {
        return httpStatus;
    }
}
