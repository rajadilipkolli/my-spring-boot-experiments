package com.example.mongoes.config;

import com.example.mongoes.web.exception.DuplicateRestaurantException;
import com.example.mongoes.web.exception.RestaurantNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRestaurantException.class)
    public Mono<ProblemDetail> handleDuplicateRestaurantException(DuplicateRestaurantException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
        return Mono.just(problemDetail);
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    Mono<ProblemDetail> handleRestaurantNotFoundException(RestaurantNotFoundException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
        return Mono.just(problemDetail);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    Mono<ProblemDetail> handleValidationErrors(WebExchangeBindException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatusCode.valueOf(400), "Request failed validation checks.");
        problemDetail.setTitle("Constraint Violation");
        List<ApiValidationError> validationErrorsList =
                ex.getAllErrors().stream()
                        .map(
                                objectError -> {
                                    FieldError fieldError = (FieldError) objectError;
                                    return new ApiValidationError(
                                            fieldError.getObjectName(),
                                            fieldError.getField(),
                                            fieldError.getRejectedValue(),
                                            Objects.requireNonNull(
                                                    fieldError.getDefaultMessage(), ""));
                                })
                        .sorted(Comparator.comparing(ApiValidationError::field))
                        .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return Mono.just(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    Mono<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Validation failed");
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.mongoes.com/errors/validation-error"));
        List<ApiValidationError> validationErrorsList =
                ex.getConstraintViolations().stream()
                        .map(
                                violation ->
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

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
