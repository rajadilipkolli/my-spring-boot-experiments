package com.example.learning.exception;

public class PostNotFoundException extends ResourceNotFoundException {

    public PostNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
