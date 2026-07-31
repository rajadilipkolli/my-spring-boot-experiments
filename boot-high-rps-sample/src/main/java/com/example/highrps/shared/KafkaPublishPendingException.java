package com.example.highrps.shared;

public class KafkaPublishPendingException extends RuntimeException {
    public KafkaPublishPendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
