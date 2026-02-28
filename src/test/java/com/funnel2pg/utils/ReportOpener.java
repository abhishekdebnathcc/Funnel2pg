package com.funnel2pg.utils;

import com.funnel2pg.config.ConfigReader;

import java.io.File;
import java.nio.file.Paths;

/**
 * Opens the Extent Report in Chrome after test execution.
 *
 * FIX for report delay:
 *   - Removed inheritIO() — that caused Maven's JVM to wait for Chrome's stdout/stderr,
 *     which blocked process exit until Chrome was closed (potentially minutes).
 *   - redirectErrorStream(false) + no inheritIO = fire-and-forget; Chrome opens
 *     immediately and Maven exits cleanly.
 */
public class ReportOpener {

    public static void openReportInChrome() {
        try {
            String reportPath = Paths.get(ConfigReader.getReportsDir(), "extent-report.html")
                    .toAbsolutePath().toString();

            File reportFile = new File(reportPath);
            if (!reportFile.exists()) {
                System.out.println("⚠ Report file not found: " + reportPath);
                return;
            }

            String fileUrl = "file://" + reportPath;
            String os = System.getProperty("os.name", "").toLowerCase();

            if (os.contains("mac")) {
                openMac(fileUrl);
            } else if (os.contains("win")) {
                openWindows(fileUrl);
            } else {
                openLinux(fileUrl);
            }
        } catch (Exception e) {
            System.out.println("⚠ Could not open report: " + e.getMessage());
        }
    }

    private static void openMac(String fileUrl) {
        try {
            // No inheritIO() — fire and forget; Maven does not wait for Chrome
            new ProcessBuilder("open", "-a", "Google Chrome", fileUrl)
                    .redirectErrorStream(false)
                    .start();
            System.out.println("✅ Report opened in Chrome");
        } catch (Exception e) {
            try {
                new ProcessBuilder("open", fileUrl)
                        .redirectErrorStream(false)
                        .start();
                System.out.println("✅ Report opened in default browser");
            } catch (Exception ex) {
                System.out.println("⚠ Could not open report browser: " + ex.getMessage());
            }
        }
    }

    private static void openWindows(String fileUrl) {
        try {
            new ProcessBuilder("cmd", "/c", "start", "chrome", fileUrl)
                    .redirectErrorStream(false)
                    .start();
            System.out.println("✅ Report opened in Chrome");
        } catch (Exception e) {
            try {
                new ProcessBuilder("cmd", "/c", "start", fileUrl)
                        .redirectErrorStream(false)
                        .start();
                System.out.println("✅ Report opened in default browser");
            } catch (Exception ex) {
                System.out.println("⚠ Could not open report: " + ex.getMessage());
            }
        }
    }

    private static void openLinux(String fileUrl) {
        try {
            new ProcessBuilder("google-chrome", fileUrl)
                    .redirectErrorStream(false)
                    .start();
            System.out.println("✅ Report opened in Chrome");
        } catch (Exception e) {
            try {
                new ProcessBuilder("xdg-open", fileUrl)
                        .redirectErrorStream(false)
                        .start();
                System.out.println("✅ Report opened in default browser");
            } catch (Exception ex) {
                System.out.println("⚠ Could not open report: " + ex.getMessage());
            }
        }
    }
}
