package com.example.keysetpagination.utils;

import com.example.keysetpagination.model.query.CriteriaGroup;
import com.example.keysetpagination.model.query.ISearchCriteria;
import com.example.keysetpagination.model.query.QueryOperator;
import com.example.keysetpagination.model.query.SearchCriteria;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SpecificationDescriber {

    /** Prevents instantiation of this utility class. */
    private SpecificationDescriber() {
        // Utility class
    }

    /**
     * Describes a criterion as lines of text, indenting group children by two spaces per level.
     *
     * @param criteria the criterion or group to describe; may be {@code null}
     * @return the description, or an empty string when the criterion is {@code null}
     */
    public static String describe(ISearchCriteria<?> criteria) {
        if (criteria == null) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        describeInternal(criteria, 0, lines);
        return String.join("\n", lines);
    }

    /**
     * Describes criteria in list order, placing each top-level criterion at the same indentation.
     *
     * @param criteriaList the criteria to describe; null entries are ignored
     * @return newline-separated descriptions, or an empty string for a null or empty list
     */
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

    /**
     * Appends a criterion's description, recursively indenting groups and skipping null criteria.
     * Unknown criterion implementations are rendered using their string representation.
     *
     * @param criteria the criterion to visit; may be {@code null}
     * @param indentLevel the nesting depth, using two spaces per level
     * @param lines the destination for the description lines
     */
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

    /**
     * Formats a leaf criterion as its field, operator, and bracketed values.
     *
     * @param sc the non-null search criterion
     * @return the formatted criterion on one line
     */
    private static String formatSearchCriteria(SearchCriteria<?> sc) {
        String field = sc.getField();
        QueryOperator operator = sc.getQueryOperator();
        String formattedValues = formatValues(sc.getValues());
        return field + " " + operator + " " + formattedValues;
    }

    /**
     * Formats values in brackets, quoting strings and escaping double quotes and newline characters.
     * Null elements are represented by the unquoted text {@code null}.
     *
     * @param values the values to format; may be {@code null}
     * @return comma-separated values in brackets, or {@code []} for a null or empty list
     */
    private static String formatValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        return values.stream()
                .map(v -> v == null ? "null" : "\"" + v.replace("\"", "\\\"").replace("\n", "\\n") + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
