package com.example.keysetpagination.utils;

import com.example.keysetpagination.model.query.SearchCriteria.QueryOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.function.TriFunction;

public class QueryOperatorHandler {

    private static final Map<QueryOperator, TriFunction<Path<?>, Object, CriteriaBuilder, Predicate>>
            operatorPredicates = Map.ofEntries(
                    Map.entry(QueryOperator.EQ, (path, value, cb) -> {
                        if (value == null) {
                            return cb.isNull(path);
                        }
                        return cb.equal(path, value);
                    }),
                    Map.entry(QueryOperator.NE, (path, value, cb) -> cb.notEqual(path, value)),
                    Map.entry(QueryOperator.IN, (path, value, cb) -> path.in((List<?>) value)),
                    Map.entry(
                            QueryOperator.GT,
                            (path, value, cb) -> cb.greaterThan((Expression<Comparable>) path, (Comparable) value)),
                    Map.entry(
                            QueryOperator.LT,
                            (path, value, cb) -> cb.lessThan((Expression<Comparable>) path, (Comparable) value)),
                    Map.entry(
                            QueryOperator.GTE,
                            (path, value, cb) ->
                                    cb.greaterThanOrEqualTo((Expression<Comparable>) path, (Comparable) value)),
                    Map.entry(
                            QueryOperator.LTE,
                            (path, value, cb) ->
                                    cb.lessThanOrEqualTo((Expression<Comparable>) path, (Comparable) value)),
                    Map.entry(
                            QueryOperator.STARTS_WITH,
                            (path, value, cb) -> cb.like((Expression<String>) path, value + "%")),
                    Map.entry(
                            QueryOperator.ENDS_WITH,
                            (path, value, cb) -> cb.like((Expression<String>) path, "%" + value)),
                    Map.entry(
                            QueryOperator.CONTAINS,
                            (path, value, cb) -> cb.like((Expression<String>) path, "%" + value + "%")),
                    Map.entry(
                            QueryOperator.LIKE,
                            (path, value, cb) -> cb.like((Expression<String>) path, "%" + value + "%")),
                    Map.entry(QueryOperator.BETWEEN, (path, value, cb) -> {
                        List<?> rangeValues = (List<?>) value;
                        if (rangeValues.size() != 2) {
                            throw new IllegalArgumentException("BETWEEN operator requires exactly 2 values");
                        }
                        if (!(rangeValues.getFirst() instanceof Comparable)
                                || !(rangeValues.get(1) instanceof Comparable)) {
                            throw new IllegalArgumentException("Values for BETWEEN operator must be Comparable");
                        }
                        // Casting to Comparable<Object> for type safety
                        Comparable<Object> lowerBound = (Comparable<Object>) rangeValues.getFirst();
                        Comparable<Object> upperBound = (Comparable<Object>) rangeValues.get(1);
                        return cb.between((Expression<Comparable<Object>>) path, lowerBound, upperBound);
                    }));

    public static Predicate getPredicate(
            QueryOperator operator, Path<?> path, Object value, CriteriaBuilder criteriaBuilder) {
        return operatorPredicates
                .getOrDefault(operator, (p, v, cb) -> {
                    throw new UnsupportedOperationException("Unsupported operator: " + operator);
                })
                .apply(path, value, criteriaBuilder);
    }
}
