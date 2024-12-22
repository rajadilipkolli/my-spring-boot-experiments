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

    String valuePrefix() default "";

    String numberFormat() default "%d";
}
