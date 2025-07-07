package com.example.plugin.strategyplugin.config;

import com.example.plugin.strategyplugin.exception.PluginNotFoundException;
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

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(MethodArgumentNotValidException methodArgumentNotValidException) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatusCode.valueOf(400), "Invalid request content.");
        problemDetail.setType(URI.create("https://api.example.com/errors/validation"));
        problemDetail.setTitle("Constraint Violation");
        List<ApiValidationError> validationErrorsList =
                methodArgumentNotValidException.getAllErrors().stream()
                        .map(
                                objectError -> {
                                    if (objectError instanceof FieldError fieldError) {
                                        return new ApiValidationError(
                                                fieldError.getObjectName(),
                                                fieldError.getField(),
                                                fieldError.getRejectedValue(),
                                                Objects.requireNonNull(
                                                        fieldError.getDefaultMessage(), ""));
                                    }
                                    return null;
                                })
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(ApiValidationError::field))
                        .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return problemDetail;
    }

    @ExceptionHandler(PluginNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(PluginNotFoundException pluginNotFoundException) {
        return pluginNotFoundException.getBody();
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
