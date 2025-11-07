package com.example.keysetpagination.config;

import com.example.keysetpagination.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail onException(MethodArgumentNotValidException methodArgumentNotValidException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.boot-data-window-pagination.com/errors/validation"));
        List<ApiValidationError> validationErrorsList = methodArgumentNotValidException.getAllErrors().stream()
                .filter(objectError -> objectError instanceof FieldError)
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
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail onException(ConstraintViolationException constraintViolationException) {
        List<ApiValidationError> apiValidationErrors = constraintViolationException.getConstraintViolations().stream()
                .map(constraintViolation -> new ApiValidationError(
                        constraintViolation.getRootBeanClass().getSimpleName(),
                        constraintViolation.getPropertyPath().toString(),
                        constraintViolation.getInvalidValue(),
                        constraintViolation.getMessage()))
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(400), constraintViolationException.getMessage());
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.boot-data-window-pagination.com/errors/validation"));
        problemDetail.setProperty("errorCategory", "Validation");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("violations", apiValidationErrors);
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail onException(Exception exception) {
        if (exception instanceof ResourceNotFoundException resourceNotFoundException) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    resourceNotFoundException.getHttpStatus(), resourceNotFoundException.getMessage());
            problemDetail.setTitle("Not Found");
            problemDetail.setType(URI.create("https://api.boot-data-window-pagination.com/errors/not-found"));
            problemDetail.setProperty("errorCategory", "Generic");
            problemDetail.setProperty("timestamp", Instant.now());
            return problemDetail;
        } else {
            return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
        }
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
