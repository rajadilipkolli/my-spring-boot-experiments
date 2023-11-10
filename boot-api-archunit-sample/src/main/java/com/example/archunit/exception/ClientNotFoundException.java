package com.example.archunit.exception;

public class ClientNotFoundException extends ResourceNotFoundException {

    public ClientNotFoundException(Long id) {
        super("Client with Id '%d' not found".formatted(id));
    }
}
