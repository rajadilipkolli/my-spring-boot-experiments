package com.example.keysetpagination.model.query;

import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

public class SearchCriteria<T> implements ISearchCriteria<T> {

    @NotNull(message = "Operator cannot be null") private QueryOperator queryOperator;

    @NotBlank(message = "Field name cannot be null or blank") private String field;

    @NotNull(message = "Values list cannot be null") @Size(min = 1, message = "Values list cannot be empty") @Valid private List<String> values;

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
    public Specification<T> toSpecification() {
        return (root, query, criteriaBuilder) -> {
            // Implement predicate creation logic based on queryOperator
            Predicate predicate = null;

            if ((CollectionUtils.isEmpty(values))
                    && getQueryOperator() != QueryOperator.IN
                    && getQueryOperator() != QueryOperator.NOT_IN) {
                throw new IllegalArgumentException(
                        "Values cannot be null or empty for operator: " + getQueryOperator());
            }

            // Fetch the field type
            Class<?> fieldType = root.get(getField()).getJavaType();

            // Convert values to the appropriate type
            List<Object> typedValues = values.stream()
                    .map(value -> convertToType(value, fieldType))
                    .toList();

            // Switch for building predicates
            return switch (getQueryOperator()) {
                case EQ ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.equal(root.get(getField()), value),
                            criteriaBuilder::and);
                case NE ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.notEqual(root.get(getField()), value),
                            criteriaBuilder::and);
                case GT ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.greaterThan(root.get(getField()), (Comparable) value),
                            criteriaBuilder::and);
                case LT ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.lessThan(root.get(getField()), (Comparable) value),
                            criteriaBuilder::and);
                case GTE ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.greaterThanOrEqualTo(root.get(getField()), (Comparable) value),
                            criteriaBuilder::and);
                case LTE ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.lessThanOrEqualTo(root.get(getField()), (Comparable) value),
                            criteriaBuilder::and);
                case LIKE, CONTAINS ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(getField())),
                                    "%" + value.toString().toLowerCase() + "%"),
                            criteriaBuilder::or);
                case STARTS_WITH ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.like(root.get(getField()), value + "%"),
                            criteriaBuilder::and);
                case ENDS_WITH ->
                    combinePredicates(
                            typedValues,
                            value -> criteriaBuilder.like(root.get(getField()), "%" + value),
                            criteriaBuilder::and);
                case BETWEEN -> {
                    if (typedValues.size() != 2) {
                        throw new IllegalArgumentException("BETWEEN operator requires exactly two values");
                    }
                    yield criteriaBuilder.between(
                            root.get(getField()), (Comparable) typedValues.getFirst(), (Comparable) typedValues.get(1));
                }
                case IN -> root.get(getField()).in(typedValues);
                case NOT_IN -> criteriaBuilder.not(root.get(getField()).in(typedValues));
                case OR -> criteriaBuilder.or(root.get(getField()).in(typedValues));
                case AND -> criteriaBuilder.and(root.get(getField()).in(typedValues));
                default -> throw new IllegalArgumentException("Unsupported operator: " + getQueryOperator());
            };
        };
    }

    private Object convertToType(String value, Class<?> fieldType) {
        try {
            if (fieldType.equals(String.class)) {
                return value;
            } else if (fieldType.equals(BigDecimal.class)) {
                return new BigDecimal(value);
            } else if (fieldType.equals(UUID.class)) {
                return UUID.fromString(value);
            } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                return Integer.valueOf(value);
            } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                return Long.valueOf(value);
            } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                return Double.valueOf(value);
            } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                return Boolean.valueOf(value);
            } else if (fieldType.equals(LocalDate.class)) {
                return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
            } else if (fieldType.equals(LocalDateTime.class)) {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                return Enum.valueOf((Class<Enum>) fieldType, value);
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + fieldType.getName());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to convert value '" + value + "' to type " + fieldType.getName(), e);
        }
    }

    private Predicate combinePredicates(
            List<Object> values,
            Function<Object, Predicate> predicateFunction,
            BiFunction<Predicate, Predicate, Predicate> combiner) {
        if (values.size() == 1) {
            return predicateFunction.apply(values.getFirst());
        }
        return values.stream()
                .map(predicateFunction)
                .reduce(combiner::apply)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No predicates could be generated from values: %s".formatted(values)));
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
