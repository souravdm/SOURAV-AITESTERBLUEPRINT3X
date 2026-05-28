package com.restfulbooker.api.auth;

import com.restfulbooker.api.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns the pre-computed Base64 Basic auth header value from config.
 * No network call is needed.
 */
public class BasicAuthStrategy implements AuthStrategy {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthStrategy.class);
    private final ConfigManager config = ConfigManager.getInstance();

    @Override
    public String getToken() {
        return config.getRequired("basic.auth.header");
    }

    @Override
    public void refresh() {
        log.debug("BasicAuthStrategy.refresh() is a no-op — credentials are static");
    }
}
