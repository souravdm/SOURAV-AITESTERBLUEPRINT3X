package com.restfulbooker.api.clients;

import com.restfulbooker.api.config.ConfigManager;
import com.restfulbooker.api.constants.HttpHeaders;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

/**
 * Factory that produces configured {@link RequestSpecification} instances.
 * All state is derived from immutable config — safe for parallel execution.
 */
public final class RequestSpecFactory {

    private RequestSpecFactory() {}

    /**
     * Returns a base spec with base URI, default JSON content type, and logging.
     */
    public static RequestSpecification getBaseSpec() {
        ConfigManager config = ConfigManager.getInstance();
        return new RequestSpecBuilder()
                .setBaseUri(config.getRequired("base.url"))
                .setContentType(HttpHeaders.APP_JSON)
                .addHeader(HttpHeaders.ACCEPT, HttpHeaders.APP_JSON)
                .log(LogDetail.ALL)
                .build();
    }

    /**
     * Returns a spec that sets Accept: application/xml for XML response tests.
     */
    public static RequestSpecification getXmlAcceptSpec() {
        ConfigManager config = ConfigManager.getInstance();
        return new RequestSpecBuilder()
                .setBaseUri(config.getRequired("base.url"))
                .setContentType(HttpHeaders.APP_JSON)
                .addHeader(HttpHeaders.ACCEPT, HttpHeaders.APP_XML)
                .log(LogDetail.ALL)
                .build();
    }

    /**
     * Returns a spec pre-loaded with the Cookie token auth header.
     */
    public static RequestSpecification getAuthSpecWithCookie(String cookieHeaderValue) {
        return new RequestSpecBuilder()
                .addRequestSpecification(getBaseSpec())
                .addHeader(HttpHeaders.COOKIE, cookieHeaderValue)
                .build();
    }

    /**
     * Returns a spec pre-loaded with the Basic Authorization header.
     */
    public static RequestSpecification getAuthSpecWithBasic(String basicHeaderValue) {
        return new RequestSpecBuilder()
                .addRequestSpecification(getBaseSpec())
                .addHeader(HttpHeaders.AUTHORIZATION, basicHeaderValue)
                .build();
    }
}
