package com.example.custom.sequence.config.db;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.hibernate.annotations.IdGeneratorType;

@IdGeneratorType(StringPrefixedNumberFormattedSequenceIdGenerator.class)
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface StringPrefixedSequence {

    /**
     * Returns the database sequence name, optionally qualified by catalog or schema.
     *
     * @return a nonblank sequence name
     */
    String sequenceName();

    String valuePrefix() default "";

    String numberFormat() default "%d";

    /**
     * Returns the initial value used to configure the sequence and optimizer.
     *
     * @return the sequence starting value, defaulting to 1
     */
    int initialValue() default 1;

    /**
     * Returns the number of identifiers allocated by each sequence fetch.
     *
     * @return a positive allocation size that must match the database sequence increment
     */
    int allocationSize() default 50;
}
