package com.example.keysetpagination.exception;

public class ActorNotFoundException extends ResourceNotFoundException {

    public ActorNotFoundException(Long id) {
        super("Actor with Id '%d' not found".formatted(id));
    }
}
