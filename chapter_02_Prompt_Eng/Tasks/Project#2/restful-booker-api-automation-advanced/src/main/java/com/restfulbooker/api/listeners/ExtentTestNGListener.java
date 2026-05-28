package com.restfulbooker.api.listeners;

import com.restfulbooker.api.reporting.ExtentManager;
import com.restfulbooker.api.reporting.ExtentTestManager;
import org.testng.*;

/**
 * TestNG listener that creates Extent test nodes and records pass/fail/skip.
 */
public class ExtentTestNGListener implements ITestListener, ISuiteListener {

    // ── ISuiteListener ────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        ExtentManager.getExtent(); // initialise report early
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentManager.flush();
    }

    // ── ITestListener ─────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getDescription();
        if (name == null || name.isBlank()) name = result.getName();
        ExtentTestManager.startTest(name, result.getMethod().getDescription());
        ExtentTestManager.log("Test started: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTestManager.getTest().pass("PASSED");
        ExtentTestManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTestManager.logFail(result.getThrowable());
        ExtentTestManager.getTest().fail("FAILED — see details above");
        ExtentTestManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTestManager.getTest().skip("SKIPPED: " + result.getName());
        ExtentTestManager.removeTest();
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
}
