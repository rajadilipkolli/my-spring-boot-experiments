package com.example.keysetpagination.model.query;

import com.blazebit.text.ParserContext;
import com.blazebit.text.SerializableFormat;
import com.example.keysetpagination.utils.FilterAttributesProvider;
import com.example.keysetpagination.utils.QueryOperatorHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;

public class SearchCriteria<T> implements ISearchCriteria<T> {

    private QueryOperator queryOperator;
    private String field;
    private List<String> values;

    @JsonIgnore
    private final FilterAttributesProvider filterAttributesProvider = new FilterAttributesProvider();

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
        if (values == null || values.isEmpty()) return null;
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
        if (values.isEmpty()) {
            values.add(low);
        } else {
            values.set(0, low);
        }
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
            values.add(null);
            values.add(high);
        } else if (values.size() > 2) {
            values = new ArrayList<>(values.subList(0, 2));
        }
        if (values.size() == 1) {
            values.add(high);
        } else {
            values.set(1, high);
        }
    }

    @Override
    public Specification<T> toSpecification(Class<T> entityType) {
        Map<String, SerializableFormat<? extends Serializable>> filterAttributes =
                filterAttributesProvider.getFilterAttributes(entityType);
        if (filterAttributes == null) {
            throw new IllegalArgumentException("No filter attributes found for entity type: " + entityType.getName());
        }

        SerializableFormat<?> format = filterAttributes.get(this.getField());
        if (format == null) {
            throw new IllegalArgumentException("Invalid field in SearchCriteria: " + this.getField());
        }

        return (root, criteriaQuery, criteriaBuilder) -> {
            try {
                Path<?> path = getPath(root, this.getField());
                Object parsedValue;
                if (QueryOperator.BETWEEN.equals(this.getQueryOperator())) {
                    List<Object> parsedValues = new ArrayList<>();
                    parsedValues.add(format.parse(this.getLow(), new MyParserContextImpl(filterAttributes)));
                    parsedValues.add(format.parse(this.getHigh(), new MyParserContextImpl(filterAttributes)));
                    parsedValue = parsedValues;
                } else if (QueryOperator.IN.equals(this.getQueryOperator())) {
                    List<Object> parsedValues = new ArrayList<>();
                    for (String val : this.getValues()) {
                        parsedValues.add(format.parse(val, new MyParserContextImpl(filterAttributes)));
                    }
                    parsedValue = parsedValues;
                } else {
                    parsedValue = format.parse(this.getValue(), new MyParserContextImpl(filterAttributes));
                }

                return QueryOperatorHandler.getPredicate(this.getQueryOperator(), path, parsedValue, criteriaBuilder);
            } catch (ParseException ex) {
                throw new RuntimeException("Parsing error for field: " + this.getField(), ex);
            }
        };
    }

    private Path<?> getPath(Root<?> root, String fieldName) {
        String[] fieldParts = fieldName.split("\\.");
        Path<?> path = root.get(fieldParts[0]);
        if (path == null) {
            throw new IllegalArgumentException("Invalid field: " + fieldParts[0]);
        }
        for (int i = 1; i < fieldParts.length; i++) {
            path = path.get(fieldParts[i]);
            if (path == null) {
                throw new IllegalArgumentException("Invalid field: " + fieldParts[i]);
            }
        }
        return path;
    }

    record MyParserContextImpl(Map<String, SerializableFormat<? extends Serializable>> contextMap)
            implements ParserContext {

        public Object getAttribute(String name) {
            return this.contextMap.get(name);
        }
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
