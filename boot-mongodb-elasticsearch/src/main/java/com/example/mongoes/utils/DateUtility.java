package com.example.mongoes.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtility {

    /**
     * Converts a {@link Date} to {@link LocalDateTime} using the system default timezone.
     *
     * @param dateToConvert the date to convert, can be null
     * @return the converted {@link LocalDateTime} or null if input is null
     */
    public static LocalDateTime convertToLocalDateViaInstant(Date dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
