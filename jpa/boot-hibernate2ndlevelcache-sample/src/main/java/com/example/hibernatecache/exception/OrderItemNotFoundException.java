package com.example.hibernatecache.exception;

public class OrderItemNotFoundException extends ResourceNotFoundException {

    public OrderItemNotFoundException(Long id) {
        super("OrderItem with Id '%d' not found".formatted(id));
    }
}
