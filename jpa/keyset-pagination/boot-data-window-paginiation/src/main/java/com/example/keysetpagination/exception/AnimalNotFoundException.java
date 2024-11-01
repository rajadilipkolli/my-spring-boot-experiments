package com.example.keysetpagination.exception;

public class AnimalNotFoundException extends ResourceNotFoundException {

    public AnimalNotFoundException(Long id) {
        super("Animal with Id '%d' not found".formatted(id));
    }
}
