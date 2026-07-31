package com.example.highrps.shared;

public class KafkaPublishException extends DomainException {
    public KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
