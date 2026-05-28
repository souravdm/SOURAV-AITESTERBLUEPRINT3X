package com.restfulbooker.api.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages per-thread {@link ExtentTest} instances for parallel-safe reporting.
 */
public final class ExtentTestManager {

    private static final Logger log = LoggerFactory.getLogger(ExtentTestManager.class);
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    private ExtentTestManager() {}

    /** Creates a new test node on the report for the current thread. */
    public static ExtentTest startTest(String testName, String description) {
        ExtentTest test = ExtentManager.getExtent().createTest(testName, description);
        testThread.set(test);
        return test;
    }

    /** Returns the ExtentTest bound to the current thread. */
    public static ExtentTest getTest() {
        return testThread.get();
    }

    /** Logs an INFO message to the current test node. */
    public static void log(String message) {
        ExtentTest t = testThread.get();
        if (t != null) t.log(Status.INFO, message);
    }

    /** Logs a request/response detail block. */
    public static void logRequest(String method, String url, String headers, String body) {
        log("<b>Request:</b> " + method + " " + url
                + "<br><b>Headers:</b> <pre>" + headers + "</pre>"
                + "<br><b>Body:</b> <pre>" + escape(body) + "</pre>");
    }

    /** Logs a response detail block. */
    public static void logResponse(int status, String headers, String body) {
        log("<b>Response Status:</b> " + status
                + "<br><b>Headers:</b> <pre>" + headers + "</pre>"
                + "<br><b>Body:</b> <pre>" + escape(body) + "</pre>");
    }

    /** Logs a failure with the exception stack trace. */
    public static void logFail(Throwable t) {
        ExtentTest test = testThread.get();
        if (test != null) {
            test.log(Status.FAIL, t);
        }
        log.error("Test failed", t);
    }

    /** Removes the test from the thread-local map (call in @AfterMethod). */
    public static void removeTest() {
        testThread.remove();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
