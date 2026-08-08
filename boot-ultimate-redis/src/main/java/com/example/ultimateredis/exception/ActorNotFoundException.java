package com.example.ultimateredis.exception;

public class ActorNotFoundException extends ResourceNotFoundException {
    public ActorNotFoundException(String id) {
        super("Actor with id " + id + " not found");
    }
}
