package com.example.jooq.r2dbc.repository.custom.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jooq.SortField;
import org.jooq.TableField;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;

public class JooqSorting {

    /** Converts Spring Data Sort to jOOQ SortField. */
    <T> List<SortField<?>> getSortFields(Sort sortSpecification, T tableType) {
        List<SortField<?>> querySortFields = new ArrayList<>();

        if (sortSpecification == null || !sortSpecification.iterator().hasNext()) {
            return querySortFields;
        }

        for (Sort.Order specifiedField : sortSpecification) {
            String sortFieldName = specifiedField.getProperty();
            Sort.Direction sortDirection = specifiedField.getDirection();

            TableField<?, ?> tableField = getTableField(sortFieldName, tableType);
            if (tableField != null) {
                querySortFields.add(convertTableFieldToSortField(tableField, sortDirection));
            }
        }

        return querySortFields;
    }

    /** Retrieves the table field dynamically based on the sort field name. */
    private <T> TableField<?, ?> getTableField(String sortFieldName, T tableType) {
        try {
            Field field = tableType.getClass().getField(sortFieldName.toUpperCase(Locale.ROOT));
            return (TableField<?, ?>) field.get(tableType);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new InvalidDataAccessApiUsageException(
                    "Could not find table field '%s' in table type '%s'"
                            .formatted(sortFieldName, tableType.getClass().getSimpleName()),
                    ex);
        }
    }

    /** Converts a TableField to a SortField based on the sorting direction. */
    private SortField<?> convertTableFieldToSortField(
            TableField<?, ?> tableField, Sort.Direction sortDirection) {
        return sortDirection == Sort.Direction.ASC ? tableField.asc() : tableField.desc();
    }
}
