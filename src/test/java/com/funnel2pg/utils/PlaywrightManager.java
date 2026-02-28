package com.funnel2pg.utils;

import com.funnel2pg.config.ConfigReader;
import com.microsoft.playwright.*;

public class PlaywrightManager {

    private static final ThreadLocal<Playwright>     playwrightTL = new ThreadLocal<>();
    private static final ThreadLocal<Browser>        browserTL    = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextTL    = new ThreadLocal<>();
    private static final ThreadLocal<Page>           pageTL       = new ThreadLocal<>();

    /**
     * Captured order data from network responses and requests.
     * Stores order ID, customer info, totals, etc. from API responses.
     * This is a Map-based approach to support capturing multiple data points.
     */
    private static final ThreadLocal<java.util.Map<String, String>> capturedOrderData = new ThreadLocal<>();

    public static void setCapturedOrderId(String id) { 
        getOrderDataMap().put("order_id", id); 
    }
    public static String getCapturedOrderId() { 
        return getOrderDataMap().getOrDefault("order_id", null); 
    }
    public static void setCapturedOrderData(String key, String value) { 
        getOrderDataMap().put(key, value); 
    }
    public static String getCapturedOrderData(String key) { 
        return getOrderDataMap().getOrDefault(key, null); 
    }
    public static java.util.Map<String, String> getCapturedOrderDataMap() { 
        return getOrderDataMap(); 
    }
    public static void clearCapturedOrderData() { 
        capturedOrderData.remove(); 
    }
    
    private static java.util.Map<String, String> getOrderDataMap() {
        if (capturedOrderData.get() == null) {
            capturedOrderData.set(new java.util.HashMap<>());
        }
        return capturedOrderData.get();
    }

    /**
     * Always launches a fresh browser instance for each scenario.
     * Never reuses an existing session.
     */
    public static void initBrowser() {
        Playwright playwright = Playwright.create();
        playwrightTL.set(playwright);

        BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                .setHeadless(ConfigReader.isHeadless())
                .setSlowMo(ConfigReader.getSlowMo());

        Browser browser;
        switch (ConfigReader.getBrowser().toLowerCase()) {
            case "firefox": browser = playwright.firefox().launch(opts); break;
            case "webkit":  browser = playwright.webkit().launch(opts);  break;
            default:        browser = playwright.chromium().launch(opts);
        }
        browserTL.set(browser);

        BrowserContext ctx = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900));
        contextTL.set(ctx);

        Page page = ctx.newPage();
        page.setDefaultTimeout(ConfigReader.getTimeout());

        // ── Network listener: capture order_id from CRM API responses ──────────
        // The checkout and upsell submissions return JSON like:
        //   {"success": true, "crm_response": {"order_id": "1234567", ...}}
        // We listen to responses and extract order_id, customer data, totals, etc.
        page.onResponse(response -> {
            try {
                String url = response.url();
                int status = response.status();
                
                // Capture from successful responses and redirects
                if ((status >= 200 && status < 300) || (status >= 300 && status < 400)) {
                    captureOrderDataFromResponse(response, url);
                }
                // Log other responses for debugging if they're API calls
                else if (status >= 400 && url.contains("api")) {
                    System.out.println("⚠ API error response [" + status + "]: " + url);
                }
            } catch (Exception ignored) {
                // Network listener must never crash the test
            }
        });
        
        // Also capture from request bodies (for diagnostics)
        page.onRequest(request -> {
            try {
                String url = request.url();
                String method = request.method();
                if ((method.equals("POST") && (url.contains("place-order") || url.contains("upsell"))) 
                    && System.getProperty("debug.requests", "false").equals("true")) {
                    System.out.println("📤 Request: " + method + " " + url);
                }
            } catch (Exception ignored) {}
        });

        pageTL.set(page);
    }

    /** Extracts order_id value from a JSON string without requiring a JSON library. */
    private static String extractOrderId(String json) {
        // Try "order_id":"1234567" or "order_id":1234567
        String[] patterns = {"\"order_id\":\"", "\"order_id\":"};
        for (String pattern : patterns) {
            int idx = json.indexOf(pattern);
            if (idx < 0) continue;
            int start = idx + pattern.length();
            // Remove leading quote if present
            if (json.charAt(start) == '"') start++;
            int end = start;
            while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            String value = json.substring(start, end).trim();
            if (!value.isEmpty() && !value.equals("null") && !value.equals("0")) {
                return value;
            }
        }
        return null;
    }

    /**
     * Enhanced response capture: extracts order_id, customer data, totals from API responses.
     * Handles JSON responses with nested structures like crm_response.order_id.
     */
    private static void captureOrderDataFromResponse(Response response, String url) {
        try {
            String contentType = response.headers().getOrDefault("content-type", "");
            if (!contentType.contains("json") && !contentType.contains("text")) return;
            
            String body = response.text();
            if (body == null || body.isEmpty()) return;
            
            // Extract order_id (highest priority)
            String orderId = extractOrderId(body);
            if (orderId != null && !orderId.isEmpty() && !orderId.equals("null") && !orderId.equals("0")) {
                if (getCapturedOrderId() == null || getCapturedOrderId().isEmpty()) {
                    setCapturedOrderId(orderId);
                    System.out.println("🔖 Order ID captured: " + orderId + " ← " + response.status() + " " + url);
                }
            }
            
            // Also capture other common fields if present
            if (body.contains("email")) {
                String email = extractJsonValue(body, "email");
                if (email != null && !email.isEmpty()) {
                    setCapturedOrderData("email", email);
                }
            }
            if (body.contains("total")) {
                String total = extractJsonValue(body, "total");
                if (total != null && !total.isEmpty()) {
                    setCapturedOrderData("total", total);
                }
            }
            if (body.contains("subtotal")) {
                String subtotal = extractJsonValue(body, "subtotal");
                if (subtotal != null && !subtotal.isEmpty()) {
                    setCapturedOrderData("subtotal", subtotal);
                }
            }
        } catch (Exception ignored) {
            // Response capture must never crash the test
        }
    }
    
    /**
     * Generic JSON value extraction for simple key-value pairs.
     * Handles both quoted and unquoted values.
     */
    private static String extractJsonValue(String json, String key) {
        String[] patterns = {"\"" + key + "\":\"", "\"" + key + ":"};
        for (String pattern : patterns) {
            int idx = json.indexOf(pattern);
            if (idx < 0) continue;
            int start = idx + pattern.length();
            if (start < json.length() && json.charAt(start) == '"') start++;
            int end = start;
            while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            String value = json.substring(start, end).trim();
            if (!value.isEmpty() && !value.equals("null")) {
                return value;
            }
        }
        return null;
    }

    public static Page           getPage()    { return pageTL.get(); }
    public static BrowserContext getContext() { return contextTL.get(); }

    /**
     * Fully closes the browser instance and all associated resources.
     * Call this only for scenarios that should NOT leave the browser open.
     */
    public static void closeBrowser() {
        clearCapturedOrderData();
        if (pageTL.get()       != null) { try { pageTL.get().close();       } catch (Exception ignored) {} pageTL.remove(); }
        if (contextTL.get()    != null) { try { contextTL.get().close();    } catch (Exception ignored) {} contextTL.remove(); }
        if (browserTL.get()    != null) { try { browserTL.get().close();    } catch (Exception ignored) {} browserTL.remove(); }
        if (playwrightTL.get() != null) { try { playwrightTL.get().close(); } catch (Exception ignored) {} playwrightTL.remove(); }
    }
}
