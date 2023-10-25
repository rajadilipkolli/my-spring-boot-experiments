package com.example.graphql.exception;

import org.springframework.http.HttpStatus;

public class PostNotFoundException extends RestControllerException {

    public PostNotFoundException(Long id) {
        super("Post: " + id + " was not found.", HttpStatus.NOT_FOUND);
    }
}
