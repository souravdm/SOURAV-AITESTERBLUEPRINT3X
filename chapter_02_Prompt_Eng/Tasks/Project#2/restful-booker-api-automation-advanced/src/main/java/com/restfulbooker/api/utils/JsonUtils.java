package com.restfulbooker.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restfulbooker.api.exceptions.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * JSON serialization / deserialization utilities.
 */
public final class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    /** Serializes an object to a JSON string. */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            throw new FrameworkException("Failed to serialize object to JSON", e);
        }
    }

    /** Deserializes a JSON string to the given type. */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new FrameworkException("Failed to deserialize JSON to " + type.getSimpleName(), e);
        }
    }

    /** Loads a JSON file from the classpath and deserializes it to the given type. */
    public static <T> T fromClasspath(String resourcePath, Class<T> type) {
        try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new FrameworkException("Classpath resource not found: " + resourcePath);
            }
            return MAPPER.readValue(is, type);
        } catch (IOException e) {
            throw new FrameworkException("Failed to load JSON from classpath: " + resourcePath, e);
        }
    }

    /** Returns the shared ObjectMapper instance (for advanced use). */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
