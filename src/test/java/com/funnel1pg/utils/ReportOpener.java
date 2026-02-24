package com.funnel1pg.utils;

import com.funnel1pg.config.ConfigReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Utility class to open test reports in Chrome browser
 * Automatically opens the Extent Report after test execution completes
 */
public class ReportOpener {

    /**
     * Open the Extent Report in Chrome browser
     * Works on macOS, Windows, and Linux
     */
    public static void openReportInChrome() {
        try {
            String reportPath = Paths.get(ConfigReader.getReportsDir(), "extent-report.html")
                    .toAbsolutePath()
                    .toString();

            File reportFile = new File(reportPath);

            // Verify report file exists
            if (!reportFile.exists()) {
                System.out.println("⚠️ Report file not found: " + reportPath);
                return;
            }

            // Get the file URL
            String fileUrl = "file://" + reportPath;

            // Detect OS and open accordingly
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("mac")) {
                // macOS
                openOnMac(fileUrl);
            } else if (osName.contains("win")) {
                // Windows
                openOnWindows(fileUrl);
            } else if (osName.contains("linux")) {
                // Linux
                openOnLinux(fileUrl);
            } else {
                System.out.println("⚠️ Unsupported OS: " + osName);
                System.out.println("📄 Please open manually: " + reportPath);
            }

        } catch (Exception e) {
            System.out.println("❌ Error opening report: " + e.getMessage());
            System.err.println("Exception details: ");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Open report on macOS
     */
    private static void openOnMac(String fileUrl) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "open",
                "-a",
                "Google Chrome",
                fileUrl
        );
        
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Report opened in Chrome");
            } else {
                // Fallback: try generic open command
                System.out.println("⚠️ Chrome not found, trying generic open...");
                Runtime.getRuntime().exec(new String[]{"open", fileUrl});
            }
        } catch (InterruptedException e) {
            System.out.println("⚠️ Process interrupted while opening report");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Open report on Windows
     */
    private static void openOnWindows(String fileUrl) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "cmd",
                "/c",
                "start chrome \"" + fileUrl + "\""
        );
        
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Report opened in Chrome");
            } else {
                System.out.println("⚠️ Could not open Chrome, trying default browser...");
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", fileUrl});
            }
        } catch (InterruptedException e) {
            System.out.println("⚠️ Process interrupted while opening report");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Open report on Linux
     */
    private static void openOnLinux(String fileUrl) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "google-chrome",
                fileUrl
        );
        
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Report opened in Chrome");
            } else {
                // Fallback to xdg-open
                System.out.println("⚠️ Chrome not found, trying xdg-open...");
                Runtime.getRuntime().exec(new String[]{"xdg-open", fileUrl});
            }
        } catch (InterruptedException e) {
            System.out.println("⚠️ Process interrupted while opening report");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Alternative method: Open using Desktop API (works across platforms)
     * This method can be used as a fallback
     */
    @SuppressWarnings("unused")
    public static void openReportUsingDesktopAPI() {
        try {
            String reportPath = Paths.get(ConfigReader.getReportsDir(), "extent-report.html")
                    .toAbsolutePath()
                    .toString();

            File reportFile = new File(reportPath);

            if (!reportFile.exists()) {
                System.out.println("⚠️ Report file not found: " + reportPath);
                return;
            }

            // Use Desktop API to open file in default browser
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(reportFile.toURI());
                    System.out.println("✅ Report opened in default browser");
                } else {
                    System.out.println("⚠️ Desktop API does not support BROWSE action");
                }
            } else {
                System.out.println("⚠️ Desktop API not supported on this platform");
            }
        } catch (Exception e) {
            System.out.println("❌ Error opening report using Desktop API: " + e.getMessage());
        }
    }
}
