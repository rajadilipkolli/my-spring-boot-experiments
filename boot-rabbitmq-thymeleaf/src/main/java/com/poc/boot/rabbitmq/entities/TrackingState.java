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

    private String correlationId;

    private String status = "processed";

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

    public String getStatus() {
        return status;
    }

    public TrackingState setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TrackingState.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("correlationId='" + correlationId + "'")
                .add("status='" + status + "'")
                .toString();
    }
}
