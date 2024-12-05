package com.example.mongoes.web.exception;

public class DuplicateRestaurantException extends RuntimeException {

    public DuplicateRestaurantException(String message) {
        super(message);
    }
}
