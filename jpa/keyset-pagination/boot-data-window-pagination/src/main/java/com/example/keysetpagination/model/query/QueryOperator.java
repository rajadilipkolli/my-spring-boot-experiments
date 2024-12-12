package com.example.keysetpagination.model.query;

/**
 * Represents the supported operators for dynamic query specifications.
 * These operators are used to build dynamic JPA queries through specifications.
 */
public enum QueryOperator {

    // Comparison Operators
    /** Equal to */
    EQ,
    /** Not equal to */
    NE,
    /** Less than */
    LT,
    /** Greater than */
    GT,
    /** Greater than or equal to */
    GTE,
    /** Less than or equal to */
    LTE,
    /** Between two values (inclusive) */
    BETWEEN,
    /** Is null check */
    IS_NULL,
    /** Is not null check */
    IS_NOT_NULL,

    // Collection Operators
    /** In a collection of values */
    IN,
    /** Not in a collection of values */
    NOT_IN,

    // String Operators
    /** SQL LIKE operation */
    LIKE,
    /** Contains substring */
    CONTAINS,
    /** Starts with prefix */
    STARTS_WITH,
    /** Ends with suffix */
    ENDS_WITH,

    // Logical Operators
    /** Logical AND */
    AND,
    /** Logical OR */
    OR;

    /**
     * Checks if the operator is applicable to string fields.
     * @return true if the operator can be used with strings
     */
    public boolean isStringOperator() {
        return this == LIKE || this == CONTAINS || this == STARTS_WITH || this == ENDS_WITH;
    }

    /**
     * Checks if the operator is a logical operator.
     * @return true if the operator is logical (AND, OR)
     */
    public boolean isLogicalOperator() {
        return this == AND || this == OR;
    }
}
