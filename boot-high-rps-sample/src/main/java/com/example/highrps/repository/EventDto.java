package com.example.highrps.repository;

import java.io.Serial;
import java.io.Serializable;

public class EventDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private Long value;

    public EventDto() {}

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
