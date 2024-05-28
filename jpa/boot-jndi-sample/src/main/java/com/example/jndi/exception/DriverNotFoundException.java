package com.example.jndi.exception;

public class DriverNotFoundException extends ResourceNotFoundException {

    public DriverNotFoundException(Long id) {
        super("Driver with Id '%d' not found".formatted(id));
    }
}
