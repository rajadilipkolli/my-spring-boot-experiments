package com.example.hibernatecache.exception;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super("Order with Id '%d' not found".formatted(id));
    }
}
