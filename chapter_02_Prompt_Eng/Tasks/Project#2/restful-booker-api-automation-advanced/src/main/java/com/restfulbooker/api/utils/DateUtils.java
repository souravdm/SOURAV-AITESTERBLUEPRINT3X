package com.restfulbooker.api.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Date utilities for generating test data in the CCYY-MM-DD format expected by the API.
 */
public final class DateUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtils() {}

    /** Returns today's date as CCYY-MM-DD. */
    public static String today() {
        return LocalDate.now().format(FMT);
    }

    /** Returns a date {@code days} from today as CCYY-MM-DD. */
    public static String daysFromNow(int days) {
        return LocalDate.now().plusDays(days).format(FMT);
    }

    /** Returns the given date minus {@code days} as CCYY-MM-DD. */
    public static String minusDays(String date, int days) {
        return LocalDate.parse(date, FMT).minusDays(days).format(FMT);
    }

    /** Returns an invalid date string for negative test scenarios. */
    public static String invalidDate() {
        return "13/13/2024";
    }
}
