package com.example.keysetpagination.services;

import com.example.keysetpagination.model.query.QueryOperator;
import com.example.keysetpagination.model.query.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class EntitySpecification<T> {

    private final EntityManager entityManager;

    public EntitySpecification(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Specification<T> specificationBuilder(SearchCriteria[] searchCriteriaArray, Class<T> entityClass) {
        validateMetadata(searchCriteriaArray, entityClass);
        return (root, query, criteriaBuilder) -> {
            // Dynamically build predicates based on filters
            return Stream.of(searchCriteriaArray)
                    .map(entry -> createPredicate(entry, root, criteriaBuilder))
                    .reduce(criteriaBuilder::and)
                    .orElse(criteriaBuilder.conjunction());
        };
    }

    private Predicate createPredicate(SearchCriteria searchCriteria, Root<T> root, CriteriaBuilder criteriaBuilder) {
        String fieldName = searchCriteria.getField();
        QueryOperator operator = searchCriteria.getQueryOperator();
        List<String> values = searchCriteria.getValues();

        if (values == null || values.isEmpty() && operator != QueryOperator.IN && operator != QueryOperator.NOTIN) {
            throw new IllegalArgumentException("Values cannot be null or empty for operator: " + operator);
        }

        // Switch for building predicates
        return switch (operator) {
            case EQ -> combinePredicates(
                    values, value -> criteriaBuilder.equal(root.get(fieldName), value), criteriaBuilder::and);
            case NE -> combinePredicates(
                    values, value -> criteriaBuilder.notEqual(root.get(fieldName), value), criteriaBuilder::and);
            case GT -> combinePredicates(
                    values, value -> criteriaBuilder.greaterThan(root.get(fieldName), value), criteriaBuilder::and);
            case LT -> combinePredicates(
                    values, value -> criteriaBuilder.lessThan(root.get(fieldName), value), criteriaBuilder::and);
            case GTE -> combinePredicates(
                    values,
                    value -> criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), value),
                    criteriaBuilder::and);
            case LTE -> combinePredicates(
                    values,
                    value -> criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), value),
                    criteriaBuilder::and);
            case LIKE, CONTAINS -> combinePredicates(
                    values, value -> criteriaBuilder.like(root.get(fieldName), "%" + value + "%"), criteriaBuilder::or);
            case STARTS_WITH -> combinePredicates(
                    values, value -> criteriaBuilder.like(root.get(fieldName), value + "%"), criteriaBuilder::and);
            case ENDS_WITH -> combinePredicates(
                    values, value -> criteriaBuilder.like(root.get(fieldName), "%" + value), criteriaBuilder::and);
            case BETWEEN -> {
                if (values.size() != 2) {
                    throw new IllegalArgumentException("BETWEEN operator requires exactly two values");
                }
                yield criteriaBuilder.between(root.get(fieldName), values.get(0), values.get(1));
            }
            case IN -> root.get(fieldName).in(values);
            case NOTIN -> criteriaBuilder.not(root.get(fieldName).in(values));
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    private Predicate combinePredicates(
            List<String> values,
            Function<String, Predicate> predicateFunction,
            BiFunction<Predicate, Predicate, Predicate> combiner) {
        return values.stream()
                .map(predicateFunction)
                .reduce(combiner::apply)
                .orElseThrow(() -> new IllegalArgumentException("No predicates could be generated from values"));
    }

    private void validateMetadata(SearchCriteria[] searchCriteriaArray, Class<T> entityClass) {
        Metamodel metamodel = entityManager.getMetamodel();
        ManagedType<T> managedType = metamodel.managedType(entityClass);

        for (SearchCriteria searchCriteria : searchCriteriaArray) {
            String fieldName = searchCriteria.getField();
            if (managedType.getAttribute(fieldName) == null) {
                throw new IllegalArgumentException("Invalid field: " + fieldName);
            }
        }
    }
}
