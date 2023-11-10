package com.example.graphql.querydsl.exception;

public class TagNotFoundException extends ResourceNotFoundException {

    public TagNotFoundException(Long id) {
        super("Tag with Id '%d' not found".formatted(id));
    }
}
