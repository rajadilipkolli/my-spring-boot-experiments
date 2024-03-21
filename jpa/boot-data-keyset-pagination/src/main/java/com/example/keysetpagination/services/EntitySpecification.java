package com.example.keysetpagination.services;

import com.blazebit.text.FormatUtils;
import com.blazebit.text.ParserContext;
import com.blazebit.text.SerializableFormat;
import com.example.keysetpagination.model.query.SearchCriteria;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class EntitySpecification<T> {

    public Specification<T> specificationBuilder(SearchCriteria[] searchCriteria) {
        if (Objects.nonNull(searchCriteria) && searchCriteria.length > 0) {
            List<Specification<T>> specifications =
                    Stream.of(searchCriteria)
                            .filter(Objects::nonNull)
                            .map(this::createSpecification)
                            .toList();

            return Specification.allOf(specifications);
        }
        return null;
    }

    private Specification<T> createSpecification(SearchCriteria searchCriteria) {
        ParserContext parserContext = new MyParserContextImpl(FILTER_ATTRIBUTES);
        return (root, criteriaQuery, criteriaBuilder) -> {
            try {
                SerializableFormat<?> format = FILTER_ATTRIBUTES.get(searchCriteria.getField());
                if (format != null) {
                    String[] fieldParts = searchCriteria.getField().split("\\.");
                    Path<?> path = root.get(fieldParts[0]);
                    for (int i = 1; i < fieldParts.length; i++) {
                        path = path.get(fieldParts[i]);
                    }
                    switch (searchCriteria.getQueryOperator()) {
                        case EQ:
                            return criteriaBuilder.equal(
                                    path, format.parse(searchCriteria.getValue(), parserContext));
                        case NE:
                            return criteriaBuilder.notEqual(
                                    path, format.parse(searchCriteria.getValue(), parserContext));
                        case GT:
                            return criteriaBuilder.greaterThan(
                                    (Expression<Comparable>) path,
                                    (Comparable)
                                            format.parse(searchCriteria.getValue(), parserContext));
                        case LT:
                            return criteriaBuilder.lessThan(
                                    (Expression<Comparable>) path,
                                    (Comparable)
                                            format.parse(searchCriteria.getValue(), parserContext));
                        case GTE:
                            return criteriaBuilder.greaterThanOrEqualTo(
                                    (Expression<Comparable>) path,
                                    (Comparable)
                                            format.parse(searchCriteria.getValue(), parserContext));
                        case LTE:
                            return criteriaBuilder.lessThanOrEqualTo(
                                    (Expression<Comparable>) path,
                                    (Comparable)
                                            format.parse(searchCriteria.getValue(), parserContext));
                        case IN:
                            List<String> values = searchCriteria.getValues();
                            List<Object> filterValues = new ArrayList<>(values.size());
                            for (String value : values) {
                                filterValues.add(format.parse(value, parserContext));
                            }
                            return path.in(filterValues);
                        case BETWEEN:
                            return criteriaBuilder.between(
                                    (Expression<Comparable>) path,
                                    (Comparable)
                                            format.parse(searchCriteria.getLow(), parserContext),
                                    (Comparable)
                                            format.parse(searchCriteria.getHigh(), parserContext));
                        case STARTS_WITH:
                            return criteriaBuilder.like(
                                    (Expression<String>) path,
                                    format.parse(searchCriteria.getValue(), parserContext) + "%");
                        case ENDS_WITH:
                            return criteriaBuilder.like(
                                    (Expression<String>) path,
                                    "%" + format.parse(searchCriteria.getValue(), parserContext));
                        case CONTAINS, LIKE:
                            return criteriaBuilder.like(
                                    (Expression<String>) path,
                                    "%"
                                            + format.parse(searchCriteria.getValue(), parserContext)
                                            + "%");
                        default:
                            throw new UnsupportedOperationException(
                                    "Unsupported Query Operator: "
                                            + searchCriteria.getQueryOperator());
                    }
                }
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        };
    }

    private static class MyParserContextImpl implements ParserContext {
        private final Map<String, SerializableFormat<? extends Serializable>> contextMap;

        private MyParserContextImpl(
                Map<String, SerializableFormat<? extends Serializable>> filterAttributes) {
            this.contextMap = filterAttributes;
        }

        public Object getAttribute(String name) {
            return this.contextMap.get(name);
        }
    }

    private static final Map<String, SerializableFormat<? extends Serializable>> FILTER_ATTRIBUTES;

    static {
        Map<String, SerializableFormat<? extends Serializable>> filterAttributes = new HashMap<>();
        filterAttributes.put("id", FormatUtils.getAvailableFormatters().get(Long.class));
        filterAttributes.put("name", FormatUtils.getAvailableFormatters().get(String.class));
        filterAttributes.put(
                "createdOn", FormatUtils.getAvailableFormatters().get(LocalDate.class));
        FILTER_ATTRIBUTES = Collections.unmodifiableMap(filterAttributes);
    }
}
