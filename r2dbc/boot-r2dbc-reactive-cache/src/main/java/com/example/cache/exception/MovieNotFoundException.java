package com.example.cache.exception;

public class MovieNotFoundException extends ResourceNotFoundException {

    public MovieNotFoundException(Long id) {
        super("Movie with Id '%d' not found".formatted(id));
    }
}
