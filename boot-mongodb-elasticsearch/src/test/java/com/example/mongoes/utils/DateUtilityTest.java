package com.example.mongoes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

class DateUtilityTest {

    @Test
    void convertToLocalDateViaInstant() {
        Date date = new Date(Integer.MAX_VALUE);
        LocalDateTime expected = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertThat(DateUtility.convertToLocalDateViaInstant(date)).isEqualTo(expected);
    }
}
