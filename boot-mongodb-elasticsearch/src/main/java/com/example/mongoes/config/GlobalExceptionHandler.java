package com.example.mongoes.config;

import com.example.mongoes.web.exception.DuplicateRestaurantException;
import com.example.mongoes.web.exception.RestaurantNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MissingRequestValueException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRestaurantException.class)
    public Mono<@NonNull ProblemDetail> handleDuplicateRestaurantException(
            DuplicateRestaurantException ex, ServerHttpRequest request) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
        problemDetail.setInstance(URI.create(request.getURI().getRawPath()));
        problemDetail.setType(URI.create("https://api.mongoes.com/errors/duplicate-restaurant"));
        return Mono.just(problemDetail);
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    Mono<@NonNull ProblemDetail> handleRestaurantNotFoundException(
            RestaurantNotFoundException ex, ServerHttpRequest request) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
        problemDetail.setInstance(URI.create(request.getURI().getRawPath()));
        problemDetail.setType(URI.create("https://api.mongoes.com/errors/restaurant-not-found"));
        return Mono.just(problemDetail);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    Mono<@NonNull ProblemDetail> handleValidationErrors(
            WebExchangeBindException ex, ServerHttpRequest request) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, "Invalid request content.");
        problemDetail.setInstance(URI.create(request.getURI().getRawPath()));
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("https://api.mongoes.com/errors/validation-error"));
        List<ApiValidationError> validationErrorsList =
                ex.getAllErrors().stream()
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
        return Mono.just(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    Mono<@NonNull ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, ServerHttpRequest request) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setInstance(URI.create(request.getURI().getRawPath()));
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.mongoes.com/errors/validation-error"));
        List<ApiValidationError> validationErrorsList =
                ex.getConstraintViolations().stream()
                        .map(violation ->
                                new ApiValidationError(
                                        violation.getRootBeanClass().getSimpleName(),
                                        violation.getPropertyPath().toString(),
                                        violation.getInvalidValue(),
                                        violation.getMessage()))
                        .sorted(Comparator.comparing(ApiValidationError::field))
                        .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return Mono.just(problemDetail);
    }

    @ExceptionHandler(MissingRequestValueException.class)
    Mono<@NonNull ProblemDetail> handleException(
            MissingRequestValueException ex, ServerHttpRequest request) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getBody().getDetail());
        problemDetail.setInstance(URI.create(request.getURI().getRawPath()));
        problemDetail.setType(URI.create("https://api.mongoes.com/errors/validation-error"));
        return Mono.just(problemDetail);
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
