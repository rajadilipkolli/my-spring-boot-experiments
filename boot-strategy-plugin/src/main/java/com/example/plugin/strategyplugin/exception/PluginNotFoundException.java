package com.example.plugin.strategyplugin.exception;

import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class PluginNotFoundException extends ErrorResponseException {

    public PluginNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Product Not Found");
        problemDetail.setType(URI.create("https://api.service.com/errors/bad-request"));
        problemDetail.setProperty("errorCategory", "Generic");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
