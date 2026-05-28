package com.restfulbooker.api.config;

import com.restfulbooker.api.exceptions.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton that loads framework.properties and the env-specific *.properties file.
 * System properties and environment variables take precedence over file values.
 * Fails fast with a clear error if a required key is missing.
 */
public final class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static volatile ConfigManager instance;

    private final Properties props = new Properties();
    private final String env;

    private ConfigManager() {
        env = resolveEnv();
        loadFile("config/framework.properties");
        loadFile("config/" + env + ".properties");
        applyEnvOverrides();
        log.info("ConfigManager initialised — env={}", env);
    }

    /** Returns the singleton instance (double-checked locking). */
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

    /** Active environment name (dev / qa / stage / prod). */
    public String getEnv() {
        return env;
    }

    /**
     * Returns the property value for {@code key}.
     *
     * @throws ConfigException if the key is absent or blank
     */
    public String getRequired(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new ConfigException("Required config key missing: " + key
                    + " — check config/" + env + ".properties or environment variables");
        }
        return value;
    }

    /** Returns the property value or {@code defaultValue} if absent. */
    public String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Returns a required integer property. */
    public int getInt(String key) {
        return Integer.parseInt(getRequired(key));
    }

    /** Returns an integer property or {@code defaultValue} if absent. */
    public int getIntOrDefault(String key, int defaultValue) {
        try {
            return Integer.parseInt(getOrDefault(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private String get(String key) {
        // System property wins, then env var, then file
        String sysProp = System.getProperty(key);
        if (sysProp != null) return sysProp;
        String envVar = System.getenv(key.replace('.', '_').toUpperCase());
        if (envVar != null) return envVar;
        return props.getProperty(key);
    }

    private String resolveEnv() {
        String e = System.getProperty("env");
        if (e == null) e = System.getenv("ENV");
        if (e == null) e = "qa";
        return e.toLowerCase();
    }

    private void loadFile(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.warn("Config file not found on classpath: {}", resourcePath);
                return;
            }
            props.load(is);
            log.debug("Loaded {}", resourcePath);
        } catch (IOException e) {
            throw new ConfigException("Failed to load config: " + resourcePath, e);
        }
    }

    private void applyEnvOverrides() {
        // Allow individual prop overrides via -Dbase.url=... etc.
        System.getProperties().forEach((k, v) -> {
            if (k instanceof String key && props.containsKey(key)) {
                props.setProperty(key, (String) v);
            }
        });
    }
}
