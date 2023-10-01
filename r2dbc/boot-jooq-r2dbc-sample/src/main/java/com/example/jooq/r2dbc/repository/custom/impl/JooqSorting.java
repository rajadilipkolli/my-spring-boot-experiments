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

    <T> List<SortField<?>> getSortFields(Sort sortSpecification, T tableType) {
        List<SortField<?>> querySortFields = new ArrayList<>();

        if (sortSpecification == null) {
            return querySortFields;
        }

        for (Sort.Order specifiedField : sortSpecification) {
            String sortFieldName = specifiedField.getProperty();
            Sort.Direction sortDirection = specifiedField.getDirection();

            TableField tableField = getTableField(sortFieldName, tableType);
            SortField<?> querySortField = convertTableFieldToSortField(tableField, sortDirection);
            querySortFields.add(querySortField);
        }

        return querySortFields;
    }

    private <T> TableField getTableField(String sortFieldName, T tableType) {
        TableField sortField;
        try {
            Field tableField =
                    tableType.getClass().getField(sortFieldName.toUpperCase(Locale.ROOT));
            sortField = (TableField) tableField.get(tableType);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            String errorMessage = String.format("Could not find table field: %s", sortFieldName);
            throw new InvalidDataAccessApiUsageException(errorMessage, ex);
        }
        return sortField;
    }

    private SortField<?> convertTableFieldToSortField(
            TableField tableField, Sort.Direction sortDirection) {
        if (sortDirection == Sort.Direction.ASC) {
            return tableField.asc();
        } else {
            return tableField.desc();
        }
    }
}
