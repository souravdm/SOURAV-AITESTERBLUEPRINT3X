package com.restfulbooker.api.utils;

import com.restfulbooker.api.config.ConfigManager;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Reusable assertion helpers wrapping REST Assured and AssertJ.
 */
public final class AssertionHelper {

    private static final Logger log = LoggerFactory.getLogger(AssertionHelper.class);

    private AssertionHelper() {}

    /** Asserts that the response has the expected HTTP status code. */
    public static void assertStatusCode(Response response, int expected) {
        Assertions.assertThat(response.statusCode())
                .as("Expected HTTP %d but got %d — body: %s", expected, response.statusCode(), response.body().asPrettyString())
                .isEqualTo(expected);
    }

    /** Asserts that the response time is within the configured SLA. */
    public static void assertResponseTimeSla(Response response) {
        long slaMills = ConfigManager.getInstance().getIntOrDefault("response.time.sla.ms", 5000);
        long actual   = response.time();
        Assertions.assertThat(actual)
                .as("Response time %d ms exceeded SLA of %d ms", actual, slaMills)
                .isLessThanOrEqualTo(slaMills);
    }

    /** Validates the response body against a JSON schema classpath resource. */
    public static void assertJsonSchema(Response response, String schemaClasspathResource) {
        InputStream schema = AssertionHelper.class.getClassLoader()
                .getResourceAsStream(schemaClasspathResource);
        if (schema == null) {
            log.warn("JSON schema not found: {} — schema validation skipped", schemaClasspathResource);
            return;
        }
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));
        log.debug("JSON schema validation passed: {}", schemaClasspathResource);
    }

    /** Asserts that a response header is present and not blank. */
    public static void assertHeaderPresent(Response response, String headerName) {
        String value = response.header(headerName);
        Assertions.assertThat(value)
                .as("Response header '%s' is missing or blank", headerName)
                .isNotBlank();
    }

    /** Asserts that a JSON path in the response is equal to the expected value. */
    public static void assertJsonPath(Response response, String jsonPath, Object expected) {
        Object actual = response.jsonPath().get(jsonPath);
        Assertions.assertThat(actual)
                .as("JSON path [%s] expected <%s> but was <%s>", jsonPath, expected, actual)
                .isEqualTo(expected);
    }

    /** Asserts that a JSON path value is not null and not blank (for Strings). */
    public static void assertJsonPathNotBlank(Response response, String jsonPath) {
        Object actual = response.jsonPath().get(jsonPath);
        Assertions.assertThat(actual)
                .as("JSON path [%s] should not be null/blank but was: %s", jsonPath, actual)
                .isNotNull();
        if (actual instanceof String s) {
            Assertions.assertThat(s).isNotBlank();
        }
    }
}
