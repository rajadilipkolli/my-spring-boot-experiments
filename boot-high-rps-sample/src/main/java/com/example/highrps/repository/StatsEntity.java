package com.example.highrps.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class StatsEntity {

    @Id
    private String id;

    @Column(name = "stat_value")
    private long value;

    protected StatsEntity() {}

    public StatsEntity(String id, long value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
