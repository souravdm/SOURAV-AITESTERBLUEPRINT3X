package com.restfulbooker.api.exceptions;

/**
 * Root unchecked exception for all framework-level failures.
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
