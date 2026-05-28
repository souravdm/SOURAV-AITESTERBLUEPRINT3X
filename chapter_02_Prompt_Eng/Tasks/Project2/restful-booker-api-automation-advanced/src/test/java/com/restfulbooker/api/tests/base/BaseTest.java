package com.restfulbooker.api.tests.base;

import com.restfulbooker.api.auth.TokenManager;
import com.restfulbooker.api.config.ConfigManager;
import com.restfulbooker.api.reporting.ExtentManager;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Base class for all tests. Configures REST Assured, warms the token cache,
 * and ensures the Extent report is flushed after the suite.
 */
public abstract class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        ConfigManager config = ConfigManager.getInstance();
        RestAssured.baseURI = config.getRequired("base.url");
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        log.info("Suite starting — env={} baseURI={}", config.getEnv(), RestAssured.baseURI);

        // Pre-warm the token so the first test doesn't pay the auth latency
        try {
            TokenManager.getInstance().getToken();
        } catch (Exception e) {
            log.warn("Token pre-warm failed (tests requiring auth may fail): {}", e.getMessage());
        }
    }

    @AfterSuite(alwaysRun = true)
    public void suiteTeardown() {
        ExtentManager.flush();
    }
}
