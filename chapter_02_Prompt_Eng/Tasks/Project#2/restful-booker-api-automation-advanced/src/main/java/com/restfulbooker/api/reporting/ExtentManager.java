package com.restfulbooker.api.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.restfulbooker.api.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages the singleton {@link ExtentReports} instance.
 * Thread-safe; call {@link #getExtent()} from any thread.
 */
public final class ExtentManager {

    private static final Logger log = LoggerFactory.getLogger(ExtentManager.class);
    private static volatile ExtentReports extent;

    private ExtentManager() {}

    /** Returns the singleton ExtentReports instance, creating it if needed. */
    public static synchronized ExtentReports getExtent() {
        if (extent == null) {
            ConfigManager config = ConfigManager.getInstance();
            String outputDir = config.getOrDefault("report.output.dir", "target/extent-report");
            String archiveDir = config.getOrDefault("report.archive.dir", "target/extent-report/archive");
            String timestamp  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // Primary report
            ExtentSparkReporter spark = new ExtentSparkReporter(outputDir + "/index.html");
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Restful-Booker API Test Report");
            spark.config().setReportName("API Automation Results");
            spark.config().setEncoding("UTF-8");
            spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

            // Timestamped archive copy
            ExtentSparkReporter archive = new ExtentSparkReporter(archiveDir + "/report_" + timestamp + ".html");
            archive.config().setTheme(Theme.DARK);
            archive.config().setDocumentTitle("Restful-Booker — " + timestamp);

            extent = new ExtentReports();
            extent.attachReporter(spark, archive);
            extent.setSystemInfo("Environment",  config.getEnv().toUpperCase());
            extent.setSystemInfo("Base URL",     config.getOrDefault("base.url", "N/A"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("OS",           System.getProperty("os.name"));

            log.info("ExtentReports initialised — output: {}/index.html", outputDir);
        }
        return extent;
    }

    /** Flushes and closes the report. Call once at end of suite. */
    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
            log.info("ExtentReports flushed");
        }
    }
}
