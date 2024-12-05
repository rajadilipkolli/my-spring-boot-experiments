package com.example.keysetpagination.services;

import com.example.keysetpagination.model.query.QueryOperator;
import com.example.keysetpagination.model.query.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EntitySpecification<T> {

    private final EntityManager entityManager;

    public EntitySpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Specification<T> specificationBuilder(List<SearchCriteria> searchCriteriaList, Class<T> entityClass) {
        validateMetadata(searchCriteriaList, entityClass);
        return (root, query, criteriaBuilder) -> {
            // Dynamically build predicates based on filters
            return searchCriteriaList.stream()
                    .map(entry -> createPredicate(entry, root, criteriaBuilder))
                    .reduce(criteriaBuilder::and)
                    .orElse(criteriaBuilder.conjunction());
        };
    }

    private Predicate createPredicate(SearchCriteria searchCriteria, Root<T> root, CriteriaBuilder criteriaBuilder) {
        String fieldName = searchCriteria.getField();
        QueryOperator operator = searchCriteria.getQueryOperator();
        List<String> values = searchCriteria.getValues();

        if ((CollectionUtils.isEmpty(values)) && operator != QueryOperator.IN && operator != QueryOperator.NOT_IN) {
            throw new IllegalArgumentException("Values cannot be null or empty for operator: " + operator);
        }

        // Fetch the field type
        Class<?> fieldType = root.get(fieldName).getJavaType();

        // Convert values to the appropriate type
        List<Object> typedValues =
                values.stream().map(value -> convertToType(value, fieldType)).toList();

        // Switch for building predicates
        return switch (operator) {
            case EQ -> combinePredicates(
                    typedValues, value -> criteriaBuilder.equal(root.get(fieldName), value), criteriaBuilder::and);
            case NE -> combinePredicates(
                    typedValues, value -> criteriaBuilder.notEqual(root.get(fieldName), value), criteriaBuilder::and);
            case GT -> combinePredicates(
                    typedValues,
                    value -> criteriaBuilder.greaterThan(root.get(fieldName), (Comparable) value),
                    criteriaBuilder::and);
            case LT -> combinePredicates(
                    typedValues,
                    value -> criteriaBuilder.lessThan(root.get(fieldName), (Comparable) value),
                    criteriaBuilder::and);
            case GTE -> combinePredicates(
                    typedValues,
                    value -> criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), (Comparable) value),
                    criteriaBuilder::and);
            case LTE -> combinePredicates(
                    typedValues,
                    value -> criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), (Comparable) value),
                    criteriaBuilder::and);
            case LIKE, CONTAINS -> combinePredicates(
                    typedValues,
                    value -> criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(fieldName)),
                            "%" + value.toString().toLowerCase() + "%"),
                    criteriaBuilder::or);
            case STARTS_WITH -> combinePredicates(
                    typedValues, value -> criteriaBuilder.like(root.get(fieldName), value + "%"), criteriaBuilder::and);
            case ENDS_WITH -> combinePredicates(
                    typedValues, value -> criteriaBuilder.like(root.get(fieldName), "%" + value), criteriaBuilder::and);
            case BETWEEN -> {
                if (typedValues.size() != 2) {
                    throw new IllegalArgumentException("BETWEEN operator requires exactly two values");
                }
                yield criteriaBuilder.between(
                        root.get(fieldName), (Comparable) typedValues.get(0), (Comparable) typedValues.get(1));
            }
            case IN -> root.get(fieldName).in(typedValues);
            case NOT_IN -> criteriaBuilder.not(root.get(fieldName).in(typedValues));
            case OR -> criteriaBuilder.or(root.get(fieldName).in(typedValues));
            case AND -> criteriaBuilder.and(root.get(fieldName).in(typedValues));
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
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
                        String.format("No predicates could be generated from values: %s", values)));
    }

    private void validateMetadata(List<SearchCriteria> searchCriteriaList, Class<T> entityClass) {
        Metamodel metamodel = entityManager.getMetamodel();
        ManagedType<T> managedType = metamodel.managedType(entityClass);

        for (SearchCriteria searchCriteria : searchCriteriaList) {
            String fieldName = searchCriteria.getField();
            if (managedType.getAttribute(fieldName) == null) {
                throw new IllegalArgumentException("Invalid field: " + fieldName);
            }
        }
    }
}
