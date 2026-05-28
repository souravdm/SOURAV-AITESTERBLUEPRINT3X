package com.restfulbooker.api.auth;

/**
 * Strategy interface for different authentication methods.
 * Implementations must be thread-safe.
 */
public interface AuthStrategy {

    /**
     * Returns the raw token / credential string.
     * For Cookie auth this is the token value; for Basic auth this is the Base64 header value.
     */
    String getToken();

    /**
     * Forces a token refresh regardless of cache state.
     */
    void refresh();
}
