package com.restfulbooker.automation.client;

import com.restfulbooker.automation.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory for reusable REST Assured specification objects.
 *
 * <p>All URIs, timeouts, and logging flags come from {@link ConfigManager} —
 * nothing is hardcoded here.  The Allure filter is attached once in the base
 * spec so every request and response is captured automatically.
 *
 * <p>Derived specs (e.g. {@link #buildPlainTextRequestSpec()}) merge the base
 * spec rather than duplicate it.  Add new endpoint-specific specs in this
 * class only — do not build specs inside service or test classes.
 */
public final class SpecBuilder {

    private static final Logger log = LogManager.getLogger(SpecBuilder.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    private SpecBuilder() {}

    // ── Request specs ────────────────────────────────────────────────────────

    /**
     * Base request specification shared by all service clients.
     * Sets the base URI and attaches the Allure capture filter.
     */
    public static RequestSpecification buildBaseRequestSpec() {
        log.debug("Building base RequestSpecification; baseURI={}", config.getBaseUri());

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUri())
                .addFilter(new AllureRestAssured());

        if (config.isLogRequest()) {
            builder.log(LogDetail.ALL);
        }

        return builder.build();
    }

    /**
     * Request specification for endpoints that return {@code text/plain}
     * (currently GET /ping).
     * Sets {@code Accept: text/plain} so the server is not asked for JSON.
     */
    public static RequestSpecification buildPlainTextRequestSpec() {
        log.debug("Building plain-text RequestSpecification");
        return new RequestSpecBuilder()
                .addRequestSpecification(buildBaseRequestSpec())
                .setAccept(ContentType.TEXT)
                .build();
    }

    /**
     * Request specification for endpoints that consume and produce JSON
     * (auth, booking CRUD).
     */
    public static RequestSpecification buildJsonRequestSpec() {
        log.debug("Building JSON RequestSpecification");
        return new RequestSpecBuilder()
                .addRequestSpecification(buildBaseRequestSpec())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    /**
     * Derives a plain-text spec with an arbitrary {@code Accept} header value
     * substituted.  Used by negative tests that send unrecognised MIME types.
     *
     * @param acceptHeader  raw value for the {@code Accept} header
     */
    public static RequestSpecification buildCustomAcceptSpec(String acceptHeader) {
        log.debug("Building custom-accept RequestSpecification; Accept={}", acceptHeader);
        return new RequestSpecBuilder()
                .addRequestSpecification(buildBaseRequestSpec())
                .addHeader("Accept", acceptHeader)
                .build();
    }

    /**
     * Builds a request spec pointed at a completely different base URI.
     * Used exclusively by negative / unreachable-host tests.
     *
     * @param baseUri  replacement base URI (must be a well-formed URI string)
     */
    public static RequestSpecification buildUnreachableHostSpec(String baseUri) {
        log.debug("Building unreachable-host RequestSpecification; baseURI={}", baseUri);
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .addFilter(new AllureRestAssured())
                .build();
    }

    // ── Response specs ───────────────────────────────────────────────────────

    /**
     * Base response specification.
     * Logs the full response when {@code log.response=true}.
     */
    public static ResponseSpecification buildBaseResponseSpec() {
        log.debug("Building base ResponseSpecification");
        ResponseSpecBuilder builder = new ResponseSpecBuilder();
        if (config.isLogResponse()) {
            builder.log(LogDetail.ALL);
        }
        return builder.build();
    }
}
