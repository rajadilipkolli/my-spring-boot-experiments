package com.example.learning.config;

import com.example.learning.exception.PostAlreadyExistsException;
import com.example.learning.exception.ResourceNotFoundException;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(MethodArgumentNotValidException methodArgumentNotValidException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        List<ApiValidationError> validationErrorsList = methodArgumentNotValidException.getAllErrors().stream()
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

    @ExceptionHandler(PostAlreadyExistsException.class)
    ProblemDetail onException(PostAlreadyExistsException postAlreadyExistsException) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                postAlreadyExistsException.getHttpStatus(), postAlreadyExistsException.getMessage());
        problemDetail.setTitle("Duplicate entry");
        problemDetail.setType(URI.create("https://api.boot-jpa-jooq.com/errors/already-exists"));
        problemDetail.setProperty("errorCategory", "Generic");
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail onException(Exception exception) {
        if (exception instanceof ResourceNotFoundException resourceNotFoundException) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    resourceNotFoundException.getHttpStatus(), resourceNotFoundException.getMessage());
            problemDetail.setTitle("Not Found");
            problemDetail.setType(URI.create("https://api.boot-jpa-jooq.com/errors/not-found"));
            problemDetail.setProperty("errorCategory", "Generic");
            problemDetail.setProperty("timestamp", Instant.now().toString());
            return problemDetail;
        } else {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatusCode.valueOf(500), "An unexpected error occurred while processing your request");
            problemDetail.setTitle("Internal Server Error");
            problemDetail.setType(URI.create("https://api.boot-jpa-jooq.com/errors/internal-error"));
            problemDetail.setProperty("errorCategory", "Generic");
            problemDetail.setProperty("timestamp", Instant.now().toString());
            // Log the actual exception for debugging
            log.error("Unexpected error", exception);
            return problemDetail;
        }
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
