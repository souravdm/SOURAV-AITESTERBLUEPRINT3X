package com.restfulbooker.automation.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Thread-safe singleton that exposes runtime configuration.
 *
 * <p>Resolution order for every key (highest priority first):
 * <ol>
 *   <li>Environment variable — key uppercased, dots/dashes → underscores
 *       (e.g. {@code base.uri} → {@code BASE_URI})</li>
 *   <li>JVM system property ({@code -Dbase.uri=…})</li>
 *   <li>Value in {@code config.properties} on the classpath</li>
 * </ol>
 *
 * <p>No credentials, URIs, or tokens are hardcoded here or in test classes.
 */
public final class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "config.properties";

    private static volatile ConfigManager instance;
    private final Properties props;

    // ── Private constructor ──────────────────────────────────────────────────

    private ConfigManager() {
        props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new IllegalStateException(
                        "Configuration file not found on classpath: " + CONFIG_FILE);
            }
            props.load(is);
            log.info("Configuration loaded from classpath:{}", CONFIG_FILE);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load configuration: " + CONFIG_FILE, e);
        }
    }

    // ── Singleton accessor ───────────────────────────────────────────────────

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    // ── Core resolution ──────────────────────────────────────────────────────

    /**
     * Resolves {@code key} by checking environment variable, then JVM system
     * property, then the properties file.
     *
     * @param key  property key as it appears in config.properties
     * @return     resolved value (never null)
     * @throws IllegalArgumentException if the key is not found in any source
     */
    public String get(final String key) {
        // 1. Environment variable (CI-friendly)
        String envKey = key.toUpperCase().replace('.', '_').replace('-', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            log.debug("Config '{}' resolved from env var '{}'", key, envKey);
            return envValue.trim();
        }

        // 2. JVM system property
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            log.debug("Config '{}' resolved from JVM system property", key);
            return sysProp.trim();
        }

        // 3. Properties file
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "Missing configuration key: '" + key + "'. " +
                    "Set it in config.properties or as env var " + envKey);
        }
        return value.trim();
    }

    // ── Typed accessors ──────────────────────────────────────────────────────

    /** Base URI for the Restful Booker API (no trailing slash). */
    public String getBaseUri() {
        return get("base.uri");
    }

    /** Response-time SLA ceiling in milliseconds (REQ-HC-004). */
    public long getResponseTimeSlaMs() {
        return Long.parseLong(get("response.time.sla.ms"));
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(get("connection.timeout"));
    }

    public int getReadTimeout() {
        return Integer.parseInt(get("read.timeout"));
    }

    public boolean isLogRequest() {
        return Boolean.parseBoolean(get("log.request"));
    }

    public boolean isLogResponse() {
        return Boolean.parseBoolean(get("log.response"));
    }

    /** Publicly documented Restful Booker test username. */
    public String getAuthUsername() {
        return get("auth.username");
    }

    /** Publicly documented Restful Booker test password. */
    public String getAuthPassword() {
        return get("auth.password");
    }
}
