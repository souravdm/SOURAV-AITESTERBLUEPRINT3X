package com.restfulbooker.api.utils;

import com.github.javafaker.Faker;

/**
 * Convenience wrapper around Faker for common test-data generation patterns.
 */
public final class FakerUtils {

    private static final Faker FAKER = new Faker();

    private FakerUtils() {}

    public static String firstName()    { return FAKER.name().firstName(); }
    public static String lastName()     { return FAKER.name().lastName(); }
    public static int    price()        { return FAKER.number().numberBetween(50, 999); }
    public static boolean depositPaid() { return FAKER.bool().bool(); }
    public static String additionalNeeds() {
        return FAKER.options().option("Breakfast", "Lunch", "Late checkout", "Early check-in", "");
    }

    /** Returns a string of the given length filled with random alphabetic characters. */
    public static String stringOfLength(int length) {
        return FAKER.lorem().characters(length, true, false);
    }
}
