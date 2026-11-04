package com.example.mongoes.web.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidVectorValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVector {
    String message() default
            "Vector must contain exactly three finite components and cannot be a zero vector";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
