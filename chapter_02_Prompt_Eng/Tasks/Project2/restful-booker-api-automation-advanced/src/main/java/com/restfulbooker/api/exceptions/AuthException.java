package com.restfulbooker.api.exceptions;

/**
 * Thrown when token acquisition or refresh fails.
 */
public class AuthException extends FrameworkException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
