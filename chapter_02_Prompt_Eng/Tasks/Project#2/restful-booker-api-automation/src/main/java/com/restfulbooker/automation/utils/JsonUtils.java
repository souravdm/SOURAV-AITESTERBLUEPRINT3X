package com.restfulbooker.automation.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Utility class for JSON serialisation / deserialisation via Jackson.
 *
 * <p>All methods throw unchecked exceptions so callers stay free of checked
 * {@code IOException} boilerplate.  The shared {@link ObjectMapper} instance
 * is thread-safe; do not replace it per-test.
 */
public final class JsonUtils {

    private static final Logger log = LogManager.getLogger(JsonUtils.class);

    /** Shared, thread-safe ObjectMapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private JsonUtils() {}

    // ── Classpath resource loading ───────────────────────────────────────────

    /**
     * Deserialises a classpath JSON resource into the given type.
     *
     * @param classpathResource  resource path relative to the classpath root
     *                           (e.g. {@code "testdata/ping.json"})
     * @param type               target class
     * @param <T>                target type
     * @return deserialised object
     * @throws RuntimeException if the resource is missing or cannot be parsed
     */
    public static <T> T readFromClasspath(String classpathResource, Class<T> type) {
        log.debug("Deserialising classpath resource '{}' as {}", classpathResource, type.getSimpleName());
        try (InputStream is = JsonUtils.class.getClassLoader()
                .getResourceAsStream(classpathResource)) {
            if (is == null) {
                throw new IllegalArgumentException(
                        "Resource not found on classpath: " + classpathResource);
            }
            return MAPPER.readValue(is, type);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to deserialise resource: " + classpathResource, e);
        }
    }

    /**
     * Deserialises a classpath JSON resource into a generic type (e.g.
     * {@code Map<String, Object>}).
     *
     * @param classpathResource  resource path relative to the classpath root
     * @param typeRef            Jackson TypeReference for the target generic type
     * @param <T>                target type
     * @return deserialised object
     */
    public static <T> T readFromClasspath(String classpathResource,
                                          TypeReference<T> typeRef) {
        log.debug("Deserialising classpath resource '{}' (generic)", classpathResource);
        try (InputStream is = JsonUtils.class.getClassLoader()
                .getResourceAsStream(classpathResource)) {
            if (is == null) {
                throw new IllegalArgumentException(
                        "Resource not found on classpath: " + classpathResource);
            }
            return MAPPER.readValue(is, typeRef);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to deserialise resource: " + classpathResource, e);
        }
    }

    /**
     * Convenience overload that returns a {@code Map<String, Object>}.
     *
     * @param classpathResource  resource path relative to the classpath root
     * @return key-value map of the JSON root object
     */
    public static Map<String, Object> readMapFromClasspath(String classpathResource) {
        return readFromClasspath(classpathResource,
                new TypeReference<Map<String, Object>>() {});
    }

    // ── Serialisation / deserialisation helpers ──────────────────────────────

    /**
     * Serialises any object to a JSON string.
     *
     * @param object  object to serialise
     * @return JSON string
     */
    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialise object to JSON", e);
        }
    }

    /**
     * Deserialises a JSON string into the given type.
     *
     * @param json  valid JSON string
     * @param type  target class
     * @param <T>   target type
     * @return deserialised object
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialise JSON string", e);
        }
    }

    /**
     * Pretty-prints an object as indented JSON (useful for Allure attachments).
     *
     * @param object  object to pretty-print
     * @return indented JSON string
     */
    public static String toPrettyJson(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to pretty-print object to JSON", e);
        }
    }

    /** Exposes the shared mapper for advanced use (e.g. REST Assured body configuration). */
    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }
}
