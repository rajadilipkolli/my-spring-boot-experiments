package com.example.mongoes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import org.junit.jupiter.api.Test;

class DateUtilityTest {

    @Test
    void convertToLocalDateViaInstant() {
        assertThat(DateUtility.convertToLocalDateViaInstant(new Date(Integer.MAX_VALUE)))
                .isEqualTo("1970-01-25T20:31:23.647");
    }
}
