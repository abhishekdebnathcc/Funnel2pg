package com.funnel2pg.hooks;

import com.aventstack.extentreports.Status;
import com.funnel2pg.config.ConfigReader;
import com.funnel2pg.utils.ExtentReportManager;
import com.funnel2pg.utils.PlaywrightManager;
import com.funnel2pg.utils.ReportOpener;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Hooks {

    @Before(order = 1)
    public void setUp(Scenario scenario) {
        System.out.println("\n====================================");
        System.out.println("SCENARIO : " + scenario.getName());
        System.out.println("  TAGS   : " + scenario.getSourceTagNames());
        System.out.println("====================================");
        PlaywrightManager.initBrowser();
        ExtentReportManager.createTest(
                scenario.getName(),
                "Tags: " + scenario.getSourceTagNames() + " | URI: " + scenario.getUri());
        try {
            Path dir = Paths.get(ConfigReader.getReportsDir(), "screenshots");
            Files.createDirectories(dir);
        } catch (Exception e) {
            System.out.println("Screenshot dir error: " + e.getMessage());
        }
    }

    @After(order = 1)
    public void tearDown(Scenario scenario) {
        var extentTest = ExtentReportManager.getTest();
        if (scenario.isFailed()) {
            if (ConfigReader.isScreenshotOnFailure()) {
                try {
                    byte[] png = PlaywrightManager.getPage()
                            .screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    scenario.attach(png, "image/png", "Failure: " + scenario.getName());
                    String safe = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_");
                    String path = ConfigReader.getReportsDir() + "/screenshots/" + safe + "_FAILED.png";
                    Files.write(Paths.get(path), png);
                    if (extentTest != null) extentTest.addScreenCaptureFromPath("screenshots/" + safe + "_FAILED.png", "Failure");
                } catch (Exception e) {
                    System.out.println("Screenshot failed: " + e.getMessage());
                }
            }
            if (extentTest != null) extentTest.log(Status.FAIL, "FAILED: " + scenario.getName());
        } else {
            if (extentTest != null) extentTest.log(Status.PASS, "PASSED");
        }
        System.out.println((scenario.isFailed() ? "FAILED" : "PASSED") + ": " + scenario.getName());
        PlaywrightManager.closeBrowser();
        System.out.println("Browser closed");
    }

    @AfterAll
    public static void afterAll() {
        ExtentReportManager.flushReports();
        System.out.println("Report saved -> reports/extent-report.html");
        ReportOpener.openReportInChrome();
    }
}
