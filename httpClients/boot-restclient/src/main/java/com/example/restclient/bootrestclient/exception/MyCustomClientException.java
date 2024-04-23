package com.example.restclient.bootrestclient.exception;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

@Getter
public class MyCustomClientException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final HttpHeaders headers;

    public MyCustomClientException(HttpStatusCode statusCode, HttpHeaders headers) {
        this.statusCode = statusCode;
        this.headers = headers;
    }
}
