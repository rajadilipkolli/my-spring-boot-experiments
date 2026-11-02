package com.example.mongoes.web.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class ValidVectorValidator implements ConstraintValidator<ValidVector, List<Float>> {

    private static final int EXPECTED_DIMENSIONS = 3;

    /**
     * Accepts exactly three non-null finite components with at least one nonzero value. A null
     * vector is invalid.
     */
    @Override
    public boolean isValid(List<Float> vector, ConstraintValidatorContext context) {
        if (vector == null) {
            return false;
        }

        if (vector.size() != EXPECTED_DIMENSIONS) {
            return false;
        }

        boolean hasNonZero = false;
        for (Float val : vector) {
            if (val == null || !Float.isFinite(val)) {
                return false;
            }
            if (val != 0.0f) {
                hasNonZero = true;
            }
        }

        return hasNonZero;
    }
}
