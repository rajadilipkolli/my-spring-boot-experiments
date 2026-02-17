package com.poc.boot.rabbitmq.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.StringJoiner;

@Entity
public class TrackingState {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String correlationId;

    private boolean ack;

    @Column(nullable = false)
    private String status = "processed";

    private String cause;

    public TrackingState() {}

    public Long getId() {
        return id;
    }

    public TrackingState setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public TrackingState setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public boolean isAck() {
        return ack;
    }

    public TrackingState setAck(boolean ack) {
        this.ack = ack;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TrackingState setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public TrackingState setCause(String cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TrackingState.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("correlationId='" + correlationId + "'")
                .add("ack=" + ack)
                .add("status='" + status + "'")
                .add("cause='" + cause + "'")
                .toString();
    }
}
