package com.restfulbooker.automation.client;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thin HTTP-method facade over REST Assured.
 *
 * <p>Service classes delegate to these static methods; tests never touch
 * {@code RestAssured.given()} directly.  Each method accepts a fully-built
 * {@link RequestSpecification} from {@link SpecBuilder} so no configuration
 * leaks into this layer.
 *
 * <p>Add new HTTP verbs here if the Restful Booker spec requires them.
 */
public final class RestClient {

    private static final Logger log = LogManager.getLogger(RestClient.class);

    private RestClient() {}

    // ── Standard HTTP verbs ──────────────────────────────────────────────────

    public static Response get(String path, RequestSpecification spec) {
        log.debug("HTTP GET    {}", path);
        return RestAssured.given()
                .spec(spec)
                .when()
                .get(path);
    }

    public static Response head(String path, RequestSpecification spec) {
        log.debug("HTTP HEAD   {}", path);
        return RestAssured.given()
                .spec(spec)
                .when()
                .head(path);
    }

    public static Response post(String path, RequestSpecification spec) {
        log.debug("HTTP POST   {}", path);
        return RestAssured.given()
                .spec(spec)
                .when()
                .post(path);
    }

    public static Response put(String path, RequestSpecification spec) {
        log.debug("HTTP PUT    {}", path);
        return RestAssured.given()
                .spec(spec)
                .when()
                .put(path);
    }

    public static Response delete(String path, RequestSpecification spec) {
        log.debug("HTTP DELETE {}", path);
        return RestAssured.given()
                .spec(spec)
                .when()
                .delete(path);
    }

    public static Response patch(String path, RequestSpecification spec) {
        log.debug("HTTP PATCH  {}", path);
        return RestAssured.given()
                .spec(spec)
                .when()
                .patch(path);
    }

    public static Response options(String path, RequestSpecification spec) {
        log.debug("HTTP OPTIONS {}", path);
        return RestAssured.given()
                .spec(spec)
                .when()
                .options(path);
    }

    /**
     * Overload for POST with a body (used by auth / booking endpoints).
     *
     * @param path  relative path (e.g. {@code /auth})
     * @param body  POJO serialised by REST Assured's Jackson integration
     * @param spec  pre-built request specification
     */
    public static Response post(String path, Object body, RequestSpecification spec) {
        log.debug("HTTP POST   {} (with body)", path);
        return RestAssured.given()
                .spec(spec)
                .body(body)
                .when()
                .post(path);
    }
}
