package com.restfulbooker.api.constants;

/**
 * All API endpoint paths. Prepend with base URL from ConfigManager.
 */
public final class Endpoints {

    private Endpoints() {}

    public static final String AUTH       = "/auth";
    public static final String BOOKING    = "/booking";
    public static final String BOOKING_ID = "/booking/{id}";
    public static final String PING       = "/ping";
}
