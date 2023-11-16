package com.example.keysetpagination.model.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActorsFilter {

    private Kind kind;
    private String field;
    private List<String> values;

    public ActorsFilter() {}

    @JsonIgnore
    public String getValue() {
        return values.getFirst();
    }

    public void setValue(String value) {
        if (values == null) {
            values = new ArrayList<>();
        } else {
            values.clear();
        }
        values.add(value);
    }

    @JsonIgnore
    public String getLow() {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }

    public void setLow(String low) {
        if (values == null) {
            values = new ArrayList<>();
        } else if (values.size() > 2) {
            values = new ArrayList<>(values.subList(0, 2));
        }
        values.set(0, low);
    }

    @JsonIgnore
    public String getHigh() {
        if (values == null || values.size() < 2) {
            return null;
        }
        return values.get(1);
    }

    public void setHigh(String high) {
        if (values == null) {
            values = new ArrayList<>();
        } else if (values.size() > 2) {
            values = new ArrayList<>(values.subList(0, 2));
        }
        values.set(1, high);
    }

    public enum Kind {
        EQ,
        LT,
        GT,
        GTE,
        LTE,
        BETWEEN,
        IN,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }
}
