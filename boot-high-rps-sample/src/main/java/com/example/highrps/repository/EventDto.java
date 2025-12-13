package com.example.highrps.repository;

import java.io.Serializable;

public class EventDto implements Serializable {

    public String id;
    public Long value;

    public EventDto() {
    }

    public EventDto(String id, Long value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
