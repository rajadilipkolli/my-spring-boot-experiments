package com.example.keysetpagination.config;

import com.example.keysetpagination.exception.ResourceNotFoundException;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(MethodArgumentNotValidException methodArgumentNotValidException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        List<ApiValidationError> validationErrorsList = methodArgumentNotValidException.getAllErrors().stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .map(fieldError -> new ApiValidationError(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()))
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail onException(Exception exception) {
        if (exception instanceof ResourceNotFoundException resourceNotFoundException) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    resourceNotFoundException.getHttpStatus(), resourceNotFoundException.getMessage());
            problemDetail.setTitle("Not Found");
            problemDetail.setType(URI.create("http://api.boot-data-keyset-pagination.com/errors/not-found"));
            problemDetail.setProperty("errorCategory", "Generic");
            problemDetail.setProperty("timestamp", Instant.now());
            return problemDetail;
        } else {
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");
            problemDetail.setTitle("Internal Server Error");
            return problemDetail;
        }
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
