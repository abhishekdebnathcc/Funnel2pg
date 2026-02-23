package com.funnel1pg.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.funnel1pg.config.ConfigReader;

import java.nio.file.Paths;

public class ExtentReportManager {

    private static ExtentReports                  extent;
    private static final ThreadLocal<ExtentTest>  testTL = new ThreadLocal<>();

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String reportPath = Paths.get(ConfigReader.getReportsDir(),
                    "extent-report.html").toString();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("Funnel1pg – Test Execution Report");
            spark.config().setReportName("Smoke Test Suite – Power Pro Heat Funnel");
            spark.config().setEncoding("utf-8");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application",  "Power Pro Heat Funnel");
            extent.setSystemInfo("Environment",  "Staging");
            extent.setSystemInfo("Base URL",     ConfigReader.getBaseUrl());
            extent.setSystemInfo("Browser",      ConfigReader.getBrowser());
            extent.setSystemInfo("Headless",     String.valueOf(ConfigReader.isHeadless()));
            extent.setSystemInfo("Tester",       System.getProperty("user.name"));
        }
        return extent;
    }

    public static void createTest(String name, String description) {
        ExtentTest test = getInstance().createTest(name, description);
        testTL.set(test);
    }

    public static ExtentTest getTest() {
        return testTL.get();
    }

    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}
