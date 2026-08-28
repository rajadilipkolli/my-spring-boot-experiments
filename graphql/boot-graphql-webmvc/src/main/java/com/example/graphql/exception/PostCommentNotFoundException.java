package com.example.graphql.exception;

import org.springframework.http.HttpStatus;

public class PostCommentNotFoundException extends RestControllerException {

    public PostCommentNotFoundException(String commentText) {
        super("PostComment: " + commentText + " was not found.", HttpStatus.NOT_FOUND);
    }

    public PostCommentNotFoundException(Long id) {
        super("PostComment: " + id + " was not found.", HttpStatus.NOT_FOUND);
    }
}
