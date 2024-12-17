package com.example.mongoes.config;

import com.example.mongoes.response.GenericMessage;
import com.example.mongoes.web.exception.DuplicateRestaurantException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRestaurantException.class)
    public ResponseEntity<GenericMessage> handleDuplicateRestaurantException(
            DuplicateRestaurantException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GenericMessage(ex.getMessage()));
    }
}
