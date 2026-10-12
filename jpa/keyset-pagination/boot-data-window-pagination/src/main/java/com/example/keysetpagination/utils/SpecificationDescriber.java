package com.example.keysetpagination.utils;

import com.example.keysetpagination.model.query.CriteriaGroup;
import com.example.keysetpagination.model.query.ISearchCriteria;
import com.example.keysetpagination.model.query.QueryOperator;
import com.example.keysetpagination.model.query.SearchCriteria;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SpecificationDescriber {

    private SpecificationDescriber() {
        // Utility class
    }

    public static String describe(ISearchCriteria<?> criteria) {
        if (criteria == null) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        describeInternal(criteria, 0, lines);
        return String.join("\n", lines);
    }

    @SuppressWarnings("rawtypes")
    public static String describe(List<? extends ISearchCriteria<?>> criteriaList) {
        if (criteriaList == null || criteriaList.isEmpty()) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        for (ISearchCriteria<?> criteria : criteriaList) {
            describeInternal(criteria, 0, lines);
        }
        return String.join("\n", lines);
    }

    @SuppressWarnings("rawtypes")
    private static void describeInternal(ISearchCriteria<?> criteria, int indentLevel, List<String> lines) {
        if (criteria == null) {
            return;
        }
        String indent = "  ".repeat(indentLevel);
        if (criteria instanceof SearchCriteria<?> sc) {
            lines.add(indent + formatSearchCriteria(sc));
        } else if (criteria instanceof CriteriaGroup cg) {
            lines.add(indent + (cg.getOperator() != null ? cg.getOperator().name() : "null"));
            List<ISearchCriteria> children = cg.getCriteriaList();
            if (children != null) {
                for (ISearchCriteria child : children) {
                    describeInternal(child, indentLevel + 1, lines);
                }
            }
        } else {
            lines.add(indent + criteria);
        }
    }

    private static String formatSearchCriteria(SearchCriteria<?> sc) {
        String field = sc.getField();
        QueryOperator operator = sc.getQueryOperator();
        String formattedValues = formatValues(sc.getValues());
        return field + " " + operator + " " + formattedValues;
    }

    private static String formatValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        return values.stream()
                .map(v -> v == null ? "null" : "\"" + v.replace("\"", "\\\"").replace("\n", "\\n") + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
