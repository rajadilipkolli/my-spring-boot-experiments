package com.example.multipledatasources.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

public class CustomServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus httpStatus;

    public CustomServiceException(String errorMessage, Throwable e, HttpStatus httpStatus) {
        super(errorMessage, e);
        this.httpStatus = httpStatus;
    }

    public CustomServiceException(String errorMessage, Throwable e) {
        this(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
