package com.restfulbooker.api.auth;

import com.restfulbooker.api.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central access point for the active {@link AuthStrategy}.
 * Chooses the strategy based on {@code auth.type} config key.
 * Thread-safe singleton.
 */
public final class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);
    private static volatile TokenManager instance;

    private final AuthStrategy strategy;

    private TokenManager() {
        String authType = ConfigManager.getInstance()
                .getOrDefault("auth.type", "COOKIE_TOKEN").toUpperCase();
        strategy = switch (authType) {
            case "BASIC" -> new BasicAuthStrategy();
            default      -> new CookieTokenStrategy();
        };
        log.info("TokenManager using strategy={}", authType);
    }

    /** Returns the singleton instance. */
    public static TokenManager getInstance() {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager();
                }
            }
        }
        return instance;
    }

    /** Returns the current token, fetching it if not yet cached. */
    public String getToken() {
        return strategy.getToken();
    }

    /** Forces a token refresh (call on 401 response). */
    public void refresh() {
        strategy.refresh();
    }

    /** Returns the Cookie header value formatted as {@code token=<value>}. */
    public String getCookieHeader() {
        return "token=" + getToken();
    }

    /** Returns the Authorization header value formatted as {@code Basic <base64>}. */
    public String getBasicAuthHeader() {
        String raw = ConfigManager.getInstance().getRequired("basic.auth.header");
        return "Basic " + raw;
    }

    /** Resets the singleton (test-support use only). */
    static synchronized void reset() {
        instance = null;
    }
}
