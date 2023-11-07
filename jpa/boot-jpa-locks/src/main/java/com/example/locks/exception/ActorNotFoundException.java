package com.example.locks.exception;

public class ActorNotFoundException extends ResourceNotFoundException {

    public ActorNotFoundException(Long id) {
        super("Actor with Id '%d' not found".formatted(id));
    }
}
