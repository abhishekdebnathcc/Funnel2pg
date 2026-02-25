package com.funnel2pg.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) throw new RuntimeException("config.properties not found");
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        return System.getProperty(key, properties.getProperty(key, "")).trim();
    }

    public static String getBaseUrl() {
        return get("base.url");
    }

    /** Landing page URL (Step 1 – prospect/shipping form) */
    public static String getLandingUrl() {
        String l = get("landing.url");
        return l.isEmpty() ? get("base.url") + "landing" : l;
    }

    /** Checkout page URL (Step 2 – payment form) */
    public static String getCheckoutUrl() {
        String c = get("checkout.url");
        return c.isEmpty() ? get("base.url") + "checkout" : c;
    }

    public static String getBrowser()             { return get("browser"); }
    public static boolean isHeadless()            { return Boolean.parseBoolean(get("headless")); }
    public static int getSlowMo()                 { return Integer.parseInt(get("slow.mo")); }
    public static int getTimeout()                { return Integer.parseInt(get("timeout")); }
    public static boolean isScreenshotOnFailure() { return Boolean.parseBoolean(get("screenshot.on.failure")); }
    public static String getReportsDir()           { return get("reports.dir"); }

    /**
     * Payment method to use on checkout.
     * Values: "creditcard" (default) | "cod" (Cash on Delivery)
     * Override via: -Dpayment.method=cod
     */
    public static String getPaymentMethod() {
        return get("payment.method").isEmpty() ? "creditcard" : get("payment.method").toLowerCase();
    }
}
