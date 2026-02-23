package com.funnel1pg.hooks;

import com.aventstack.extentreports.Status;
import com.funnel1pg.config.ConfigReader;
import com.funnel1pg.utils.ExtentReportManager;
import com.funnel1pg.utils.PlaywrightManager;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class Hooks {

    @Before(order = 1)
    public void setUp(Scenario scenario) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("▶ SCENARIO : " + scenario.getName());
        System.out.println("  TAGS     : " + scenario.getSourceTagNames());
        System.out.println("══════════════════════════════════════════");

        PlaywrightManager.initBrowser();

        // Init ExtentTest for this scenario
        String tags = scenario.getSourceTagNames().toString();
        ExtentReportManager.createTest(scenario.getName(),
                "Tags: " + tags + " | Feature: " + scenario.getUri());

        // Ensure reports/screenshots dir exists
        try {
            Path dir = Paths.get(ConfigReader.getReportsDir(), "screenshots");
            Files.createDirectories(dir);
        } catch (Exception e) {
            System.out.println("⚠ Could not create screenshots dir: " + e.getMessage());
        }
    }

    @After(order = 1)
    public void tearDown(Scenario scenario) {
        var extentTest = ExtentReportManager.getTest();

        if (scenario.isFailed()) {
            // Screenshot embedded in Cucumber HTML report
            if (ConfigReader.isScreenshotOnFailure()) {
                try {
                    byte[] png = PlaywrightManager.getPage()
                            .screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    scenario.attach(png, "image/png",
                            "Failure screenshot: " + scenario.getName());

                    // Also save to disk and log in Extent
                    String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_");
                    String filePath = ConfigReader.getReportsDir()
                            + "/screenshots/" + safeName + "_FAILED.png";
                    Files.write(Paths.get(filePath), png);

                    if (extentTest != null) {
                        extentTest.addScreenCaptureFromPath(filePath, "Failure Screenshot");
                    }
                    System.out.println("📸 Failure screenshot saved: " + filePath);
                } catch (Exception e) {
                    System.out.println("⚠ Screenshot failed: " + e.getMessage());
                }
            }
            if (extentTest != null) extentTest.log(Status.FAIL, "Scenario FAILED: " + scenario.getName());
        } else {
            if (extentTest != null) extentTest.log(Status.PASS, "Scenario PASSED");
        }

        String icon = scenario.isFailed() ? "❌ FAILED" : "✅ PASSED";
        System.out.println(icon + " : " + scenario.getName() + "\n");

        PlaywrightManager.closeBrowser();
    }

    @AfterAll
    public static void afterAll() {
        ExtentReportManager.flushReports();
        System.out.println("📊 Extent report flushed to reports/extent-report.html");
    }
}
