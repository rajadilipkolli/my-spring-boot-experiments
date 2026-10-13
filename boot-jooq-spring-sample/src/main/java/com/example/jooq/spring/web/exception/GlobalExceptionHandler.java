package com.example.jooq.spring.web.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Not Found");
        problemDetail.setType(URI.create("https://api.boot-jooq-spring.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setType(URI.create("https://api.boot-jooq-spring.com/errors/conflict"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.boot-jooq-spring.com/errors/validation-error"));
        List<ApiValidationError> validationErrorsList = ex.getAllErrors().stream()
                .map(objectError -> {
                    FieldError fieldError = (FieldError) objectError;
                    return new ApiValidationError(
                            fieldError.getObjectName(),
                            fieldError.getField(),
                            fieldError.getRejectedValue(),
                            Objects.requireNonNull(fieldError.getDefaultMessage(), ""));
                })
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
