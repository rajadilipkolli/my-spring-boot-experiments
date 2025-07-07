package com.example.keysetpagination.model.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

public class SearchCriteria {

    private QueryOperator queryOperator;
    private String field;
    private List<String> values;

    public SearchCriteria() {}

    public QueryOperator getQueryOperator() {
        return queryOperator;
    }

    public void setQueryOperator(QueryOperator queryOperator) {
        this.queryOperator = queryOperator;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

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

    public enum QueryOperator {
        EQ,
        NE,
        LT,
        GT,
        GTE,
        LTE,
        BETWEEN,
        IN,
        LIKE,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }
}
