package com.example.multipledatasources.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus httpStatus;

    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
