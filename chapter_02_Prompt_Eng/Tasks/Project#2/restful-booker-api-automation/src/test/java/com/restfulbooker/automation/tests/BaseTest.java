package com.restfulbooker.automation.tests;

import com.restfulbooker.automation.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Abstract base class for all API test classes.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate runtime configuration at suite start — fail fast before any
 *       test runs if the base URI or SLA is misconfigured.</li>
 *   <li>Expose {@link #config} and {@link #log} as protected fields so
 *       subclasses can access them without boilerplate.</li>
 *   <li>Emit suite-level start / stop markers to the log for CI readability.</li>
 * </ul>
 *
 * <p><b>What this class does NOT do:</b>
 * <ul>
 *   <li>Set {@code RestAssured.baseURI} globally — each spec is self-contained
 *       and built by {@link com.restfulbooker.automation.client.SpecBuilder}.</li>
 *   <li>Add global REST Assured filters — the Allure filter is already attached
 *       per spec in {@code SpecBuilder.buildBaseRequestSpec()}.</li>
 * </ul>
 */
public abstract class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);
    protected static final ConfigManager config = ConfigManager.getInstance();

    // ── Suite lifecycle ──────────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  Restful Booker API Automation Suite — START         ║");
        log.info("╚══════════════════════════════════════════════════════╝");
        log.info("  Base URI  : {}", config.getBaseUri());
        log.info("  SLA (ms)  : {}", config.getResponseTimeSlaMs());
        log.info("  Log Req   : {}", config.isLogRequest());
        log.info("  Log Resp  : {}", config.isLogResponse());

        validateConfiguration();
    }

    @AfterSuite(alwaysRun = true)
    public void globalTeardown() {
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  Restful Booker API Automation Suite — END           ║");
        log.info("╚══════════════════════════════════════════════════════╝");
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Validates mandatory configuration keys before any test runs.
     * Throws {@link IllegalStateException} if the base URI is blank so the
     * suite fails immediately rather than producing 12 confusing 404s.
     */
    private void validateConfiguration() {
        String baseUri = config.getBaseUri();
        if (baseUri == null || baseUri.isBlank()) {
            throw new IllegalStateException(
                    "base.uri is not configured. " +
                    "Set it in config.properties or via BASE_URI environment variable.");
        }
        if (config.getResponseTimeSlaMs() <= 0) {
            throw new IllegalStateException(
                    "response.time.sla.ms must be a positive integer.");
        }
        log.info("Configuration validated — suite is safe to proceed.");
    }
}
