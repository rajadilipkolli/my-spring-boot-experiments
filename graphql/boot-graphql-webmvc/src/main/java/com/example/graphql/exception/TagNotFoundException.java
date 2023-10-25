package com.example.graphql.exception;

import org.springframework.http.HttpStatus;

public class TagNotFoundException extends RestControllerException {

    public TagNotFoundException(String tagName) {
        super("Tag: " + tagName + " was not found.", HttpStatus.NOT_FOUND);
    }

    public TagNotFoundException(Long id) {
        super("Tag: " + id + " was not found.", HttpStatus.NOT_FOUND);
    }
}
