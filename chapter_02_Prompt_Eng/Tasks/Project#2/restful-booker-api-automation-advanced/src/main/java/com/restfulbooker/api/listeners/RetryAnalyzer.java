package com.restfulbooker.api.listeners;

import com.restfulbooker.api.config.ConfigManager;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to {@code retry.count} times (from framework.properties).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private final int maxRetry = ConfigManager.getInstance()
            .getIntOrDefault("retry.count", 1);

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < maxRetry) {
            attempt++;
            return true;
        }
        return false;
    }
}
