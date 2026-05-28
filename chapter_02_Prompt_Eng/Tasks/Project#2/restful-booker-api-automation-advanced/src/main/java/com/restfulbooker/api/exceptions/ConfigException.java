package com.restfulbooker.api.exceptions;

/**
 * Thrown when a required configuration key is missing or invalid.
 */
public class ConfigException extends FrameworkException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
