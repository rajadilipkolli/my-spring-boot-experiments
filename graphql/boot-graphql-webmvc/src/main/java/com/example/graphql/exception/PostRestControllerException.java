package com.example.graphql.exception;

import org.springframework.http.HttpStatus;

public class PostRestControllerException extends RestControllerException {

    public PostRestControllerException(Long id) {
        super("Post: " + id + " was not found.", HttpStatus.NOT_FOUND);
    }
}
