package com.example.keysetpagination.model.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.StringJoiner;

public class SearchCriteria {

    @NotNull(message = "Operator cannot be null") private QueryOperator queryOperator;

    @NotBlank(message = "Field name cannot be null or blank")
    private String field;

    @NotNull(message = "Values list cannot be null") @Size(min = 1, message = "Values list cannot be empty")
    @Valid
    private List<String> values;

    public SearchCriteria() {}

    public SearchCriteria(QueryOperator queryOperator, String field, List<String> values) {
        this.queryOperator = queryOperator;
        this.field = field;
        this.values = values;
    }

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

    @Override
    public String toString() {
        return new StringJoiner(", ", SearchCriteria.class.getSimpleName() + "[", "]")
                .add("queryOperator=" + queryOperator)
                .add("field='" + field + "'")
                .add("values=" + values)
                .toString();
    }
}
