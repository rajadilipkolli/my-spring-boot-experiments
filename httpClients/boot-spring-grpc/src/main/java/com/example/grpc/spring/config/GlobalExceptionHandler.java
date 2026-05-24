package com.example.grpc.spring.config;

import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ProblemDetail> handleGrpcException(StatusRuntimeException ex) {
        return switch (ex.getStatus().getCode()) {
            case NOT_FOUND ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(
                                    ProblemDetail.forStatusAndDetail(
                                            HttpStatus.NOT_FOUND, ex.getMessage()));
            case INVALID_ARGUMENT ->
                    ResponseEntity.badRequest()
                            .body(
                                    ProblemDetail.forStatusAndDetail(
                                            HttpStatus.BAD_REQUEST, ex.getMessage()));
            default ->
                    ResponseEntity.internalServerError()
                            .body(
                                    ProblemDetail.forStatusAndDetail(
                                            HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        };
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
