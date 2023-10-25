package com.example.graphql.exception;

import org.springframework.http.HttpStatus;

public class AuthorRestControllerException extends RestControllerException {

    public AuthorRestControllerException(Long id) {
        super("Author: " + id + " was not found.", HttpStatus.NOT_FOUND);
    }

    public AuthorRestControllerException(String email) {
        super("Author: " + email + " was not found.", HttpStatus.NOT_FOUND);
    }
}
