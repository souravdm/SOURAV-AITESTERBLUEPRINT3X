package com.restfulbooker.api.constants;

/**
 * Common HTTP header names and MIME type values used across the framework.
 */
public final class HttpHeaders {

    private HttpHeaders() {}

    public static final String CONTENT_TYPE   = "Content-Type";
    public static final String ACCEPT         = "Accept";
    public static final String COOKIE         = "Cookie";
    public static final String AUTHORIZATION  = "Authorization";

    public static final String APP_JSON       = "application/json";
    public static final String APP_XML        = "application/xml";
    public static final String TEXT_XML       = "text/xml";
    public static final String FORM_URLENC    = "application/x-www-form-urlencoded";
}
