package com.example.keysetpagination.model.query;

import java.util.Objects;

public class SortRequest {

    private String field;
    private String direction;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "SortRequest{field='%s', direction='%s'}".formatted(field, direction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SortRequest that)) return false;
        return Objects.equals(field, that.field) && Objects.equals(direction, that.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, direction);
    }
}
