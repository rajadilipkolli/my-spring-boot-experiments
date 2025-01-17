package com.example.mongoes.config;

import com.example.mongoes.response.GenericMessage;
import com.example.mongoes.web.exception.DuplicateRestaurantException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRestaurantException.class)
    public ResponseEntity<GenericMessage> handleDuplicateRestaurantException(
            DuplicateRestaurantException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GenericMessage(ex.getMessage()));
    }

    record ValidationError(String field, String message) {}

    record ErrorResponse(String error, List<ValidationError> details) {}

    @ExceptionHandler(WebExchangeBindException.class)
    Mono<ResponseEntity<ErrorResponse>> handleValidationErrors(WebExchangeBindException ex) {
        List<ValidationError> validationErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                error ->
                                        new ValidationError(
                                                error.getField(), error.getDefaultMessage()))
                        .toList();

        return Mono.just(
                ResponseEntity.badRequest()
                        .body(new ErrorResponse("Validation failed", validationErrors)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    Mono<ResponseEntity<ErrorResponse>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationError> validationErrors =
                ex.getConstraintViolations().stream()
                        .map(
                                violation ->
                                        new ValidationError(
                                                violation.getPropertyPath().toString(),
                                                violation.getMessage()))
                        .toList();

        return Mono.just(
                ResponseEntity.badRequest()
                        .body(new ErrorResponse("Validation failed", validationErrors)));
    }
}
