package com.example.highrps.shared;

public class KafkaPublishPendingException extends KafkaPublishException {
    public KafkaPublishPendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
