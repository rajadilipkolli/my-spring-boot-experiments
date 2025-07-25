package com.example.rest.proxy.exception;

import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class PostNotFoundException extends ErrorResponseException {

    public PostNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, asProblemDetail(id), null);
    }

    private static ProblemDetail asProblemDetail(Long id) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Post with Id '%d' not found".formatted(id));
        problemDetail.setTitle("Post Not Found");
        problemDetail.setType(URI.create("http://api.posts.com/errors/not-found"));
        problemDetail.setProperty("errorCategory", "Generic");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
