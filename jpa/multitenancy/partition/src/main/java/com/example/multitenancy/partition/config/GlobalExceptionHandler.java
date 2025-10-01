package com.example.multitenancy.partition.config;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail onException(MethodArgumentNotValidException methodArgumentNotValidException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        List<ApiValidationError> validationErrorsList = methodArgumentNotValidException.getAllErrors().stream()
                .map(objectError -> {
                    if (objectError instanceof FieldError fieldError) {
                        return new ApiValidationError(
                                fieldError.getObjectName(),
                                fieldError.getField(),
                                fieldError.getRejectedValue(),
                                Objects.requireNonNullElse(fieldError.getDefaultMessage(), ""));
                    } else {
                        return new ApiValidationError(
                                objectError.getObjectName(),
                                null,
                                null,
                                Objects.requireNonNullElse(objectError.getDefaultMessage(), ""));
                    }
                })
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setType(URI.create("https://multitenancy.com/errors/validation-error"));
        problemDetail.setProperty("violations", validationErrorsList);
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail onConstraintValidationException(ConstraintViolationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation error");
        problemDetail.setTitle("Constraint Violation");

        List<ApiValidationError> validationErrors = e.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String field = propertyPath.contains(".")
                            ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                            : propertyPath;

                    return new ApiValidationError(
                            violation.getRootBeanClass().getSimpleName(),
                            field,
                            violation.getInvalidValue(),
                            violation.getMessage());
                })
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setType(URI.create("https://multitenancy.com/errors/validation-error"));
        problemDetail.setProperty("violations", validationErrors);
        return problemDetail;
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
