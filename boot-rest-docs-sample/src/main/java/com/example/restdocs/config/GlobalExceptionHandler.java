package com.example.restdocs.config;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(MethodArgumentNotValidException methodArgumentNotValidException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setType(URI.create("https://api.restdocs.com/errors/constraint-violation"));
        List<ApiValidationError> validationErrorsList = methodArgumentNotValidException.getAllErrors().stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .map(objectError -> new ApiValidationError(
                        objectError.getObjectName(),
                        objectError.getField(),
                        objectError.getRejectedValue(),
                        Objects.requireNonNull(objectError.getDefaultMessage(), "")))
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return problemDetail;
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
