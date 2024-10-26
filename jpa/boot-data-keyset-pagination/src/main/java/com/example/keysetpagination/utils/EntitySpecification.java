package com.example.keysetpagination.utils;

import com.blazebit.text.ParserContext;
import com.blazebit.text.SerializableFormat;
import com.example.keysetpagination.model.query.SearchCriteria;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;

public class EntitySpecification<T> {

    private final FilterAttributesProvider filterAttributesProvider =
            new FilterAttributesProvider();

    public Specification<T> specificationBuilder(
            SearchCriteria[] searchCriteria, Class<T> entityType) {
        if (Objects.nonNull(searchCriteria) && searchCriteria.length > 0) {
            List<Specification<T>> specifications =
                    Stream.of(searchCriteria)
                            .filter(Objects::nonNull)
                            .map(sc -> createSpecification(sc, entityType))
                            .toList();
            return Specification.allOf(specifications);
        }
        return null;
    }

    private Specification<T> createSpecification(
            SearchCriteria searchCriteria, Class<T> entityType) {
        Map<String, SerializableFormat<? extends Serializable>> filterAttributes =
                filterAttributesProvider.getFilterAttributes(entityType);

        SerializableFormat<?> format = filterAttributes.get(searchCriteria.getField());
        if (format == null) {
            throw new IllegalArgumentException(
                    "Invalid field in SearchCriteria: " + searchCriteria.getField());
        }

        return (root, criteriaQuery, criteriaBuilder) -> {
            try {
                Path<?> path = getPath(root, searchCriteria.getField());
                Object parsedValue =
                        format.parse(
                                searchCriteria.getValue(),
                                new MyParserContextImpl(filterAttributes));

                return QueryOperatorHandler.getPredicate(
                        searchCriteria.getQueryOperator(), path, parsedValue, criteriaBuilder);
            } catch (ParseException ex) {
                throw new RuntimeException(
                        "Parsing error for field: " + searchCriteria.getField(), ex);
            }
        };
    }

    private Path<?> getPath(Root<?> root, String fieldName) {
        String[] fieldParts = fieldName.split("\\.");
        Path<?> path = root.get(fieldParts[0]);
        for (int i = 1; i < fieldParts.length; i++) {
            path = path.get(fieldParts[i]);
        }
        return path;
    }

    private record MyParserContextImpl(
            Map<String, SerializableFormat<? extends Serializable>> contextMap)
            implements ParserContext {

        public Object getAttribute(String name) {
            return this.contextMap.get(name);
        }
    }
}
