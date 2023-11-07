package com.example.locks.exception;

public class MovieNotFoundException extends ResourceNotFoundException {

    public MovieNotFoundException(Long id) {
        super("Movie with Id '%d' not found".formatted(id));
    }
}
