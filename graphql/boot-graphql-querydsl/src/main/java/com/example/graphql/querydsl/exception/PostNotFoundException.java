package com.example.graphql.querydsl.exception;

public class PostNotFoundException extends ResourceNotFoundException {

    public PostNotFoundException(Long id) {
        super("Post with Id '%d' not found".formatted(id));
    }
}
