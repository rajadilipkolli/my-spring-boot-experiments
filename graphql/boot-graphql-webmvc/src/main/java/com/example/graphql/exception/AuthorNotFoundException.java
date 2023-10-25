package com.example.graphql.exception;

import org.springframework.http.HttpStatus;

public class AuthorNotFoundException extends RestControllerException {

    public AuthorNotFoundException(Long id) {
        super("Author: " + id + " was not found.", HttpStatus.NOT_FOUND);
    }

    public AuthorNotFoundException(String email) {
        super("Author: " + email + " was not found.", HttpStatus.NOT_FOUND);
    }
}
