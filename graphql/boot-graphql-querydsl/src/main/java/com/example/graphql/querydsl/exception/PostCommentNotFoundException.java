package com.example.graphql.querydsl.exception;

public class PostCommentNotFoundException extends ResourceNotFoundException {

    public PostCommentNotFoundException(Long id) {
        super("PostComment with Id '%d' not found".formatted(id));
    }
}
