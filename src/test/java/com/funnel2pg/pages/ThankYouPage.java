package com.funnel2pg.pages;

import com.funnel2pg.utils.PlaywrightManager;
import com.microsoft.playwright.Page;

/**
 * ThankYouPage – selectors verified against live DOM of /2pgCCF25Feb/thank-you
 *
 * KEY DOM FACTS (from HTML inspection):
 *
 *  ORDER ID:
 *    <p id="OrderID"><span id="order_id_holder d-inline-block"></span></p>
 *    ↑ The span has a SPACE in its id — standard CSS selector won't match it.
 *    JS sets it via: $("#order_id_holder").html(orderId)   ← jQuery partial match
 *    We use: [id^="order_id_holder"]  (starts-with) to handle the malformed id.
 *    Fallback: read #OrderID text or parse from URL query param.
 *
 *  SHIPPING ADDRESS:
 *    JS reads from localStorage "prospectData" and appends to:
 *      .shipping-info li:nth-child(1..9)
 *    The <span id="firstName"> etc. are EMPTY — JS uses .append() on the <li>.
 *    We read from the <li> directly, stripping the label prefix.
 *
 *  PRODUCTS / PRICES:
 *    sessionStorage["cart"] → rendered into:
 *      #items-ordered-list  (ol > li per product)
 *      #products-div        (.summry_row per product)
 *      #product_price_total  subtotal
 *      #shipping_price_total shipping
 *      #grand_total          total
 *
 *  REPORT DELAY FIX:
 *    waitForPageToPopulate() no longer does repeated page.waitForFunction() polls
 *    with 5s timeouts that stack up. It does a single smart wait.
 */
public class ThankYouPage extends BasePage {

    public ThankYouPage(Page page) { super(page); }

    // ── Detection ─────────────────────────────────────────────────────────────

    public boolean isThankYouPageDisplayed() {
        try {
            String pt = page.getAttribute("meta[name='page-type']", "content");
            if ("thank-you".equalsIgnoreCase(pt)) return true;
        } catch (Exception ignored) {}
        try {
            if (getCurrentUrl().toLowerCase().contains("/thank-you")) return true;
        } catch (Exception ignored) {}
        try {
            if (page.locator("#OrderID").count() > 0) return true;
        } catch (Exception ignored) {}
        return false;
    }

    public String getHeading() {
        try { return page.locator("h1, h2").first().textContent().trim(); }
        catch (Exception e) { return "(no heading)"; }
    }

    // ── Wait for JS to populate the page ─────────────────────────────────────

    /**
     * Waits for the thank-you page JS to finish rendering.
     *
     * The page JS runs on DOMContentLoaded:
     *   1. Reads order_id from URL ?order_id= and sets #order_id_holder via jQuery
     *   2. Reads sessionStorage["cart"] and renders products + prices
     *   3. Reads localStorage["prospectData"] / ["checkoutData"] and populates address
     *
     * Strategy: wait for networkidle (all JS loaded), then a fixed 2s buffer.
     * Avoids multiple 5s waitForFunction polls that caused the delay.
     */
    public void waitForPageToPopulate() {
        // Wait for all JS (product rendering from sessionStorage, address population) to finish.
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(10_000));
        } catch (Exception ignored) {}
        // Buffer for #products-div JS render (fired from custom events / sessionStorage)
        page.waitForTimeout(2000);
    }

    // ── Order ID ──────────────────────────────────────────────────────────────

    /**
     * Order ID retrieval priority:
     *  1. Network-captured from CRM API response (most reliable — set in PlaywrightManager)
     *  2. #OrderID paragraph text (set by jQuery after page load)
     *  3. [id^="order_id_holder"] span (handles the malformed id with space)
     *  4. ?order_id= URL query param read via JS
     */
    public String getOrderNumber() {
        // 1. Network-captured
        String networkId = PlaywrightManager.getCapturedOrderId();
        if (networkId != null && !networkId.isEmpty()) {
            return networkId;
        }

        // 2. #OrderID paragraph
        try {
            String text = page.locator("#OrderID").textContent().trim();
            if (!text.isEmpty()) return text;
        } catch (Exception ignored) {}

        // 3. Span with malformed id (id has a space — use starts-with selector)
        try {
            String text = page.locator("[id^='order_id_holder']").textContent().trim();
            if (!text.isEmpty()) return text;
        } catch (Exception ignored) {}

        // 4. URL param
        try {
            Object val = page.evaluate(
                "(function(){ return new URLSearchParams(window.location.search).get('order_id'); })()"
            );
            if (val != null && !val.toString().isEmpty() && !val.toString().equals("null")) {
                return val.toString();
            }
        } catch (Exception ignored) {}

        return "N/A";
    }

    // ── Prices ────────────────────────────────────────────────────────────────

    public String getSubtotal() {
        return getElText("#product_price_total");
    }

    public String getShippingTotal() {
        return getElText("#shipping_price_total");
    }

    public String getOrderPrice() {
        return getElText("#grand_total");
    }

    // ── Shipping Address ──────────────────────────────────────────────────────
    // JS uses .append() on the <li> elements, so we read the full <li> text
    // and strip the label prefix (e.g. "First Name: John" → "John").

    // Shipping-info li order (verified against live DOM):
    //   1 = First Name  2 = Last Name  3 = Address
    //   4 = Country     5 = State      6 = Zip
    //   7 = Phone       8 = Email (value in <a> tag)
    // City is NOT a separate li in this funnel.

    public String getFullName() {
        String first = getLiValue(".shipping-info", 1);
        String last  = getLiValue(".shipping-info", 2);
        return (first + " " + last).trim();
    }

    public String getStreetAddress() {
        return getLiValue(".shipping-info", 3);
    }

    public String getCity() {
        return "N/A"; // not a separate li in this funnel's thank-you DOM
    }

    public String getState() {
        return getLiValue(".shipping-info", 5);
    }

    public String getZip() {
        return getLiValue(".shipping-info", 6);
    }

    public String getPhone() {
        return getLiValue(".shipping-info", 7);
    }

    public String getEmail() {
        // Email value lives inside the <a> tag in li:nth-child(8)
        try {
            String text = page.locator(".shipping-info li:nth-child(8) a").textContent().trim();
            if (!text.isEmpty()) return text;
        } catch (Exception ignored) {}
        return getLiValue(".shipping-info", 8);
    }

    public String getCountry() {
        // li:nth-child(4) = Country (appended by JS from prospectData.shippingCountry via getCountryByCode)
        return getLiValue(".shipping-info", 4);
    }

    public String getShippingAddress() {
        return getStreetAddress() + ", " + getState() + " " + getZip();
    }

    // ── Products ──────────────────────────────────────────────────────────────

    /** Raw text of all ordered items. */
    public String getOrderItems() {
        // Products rendered into #products-div as .summry_row divs by JS
        try {
            String text = page.locator("#products-div").textContent().trim().replaceAll("\\s+", " ");
            if (!text.isEmpty()) return text;
        } catch (Exception ignored) {}
        return "N/A";
    }

    /**
     * Returns each product as "Name  $Price" from #products-div .summry_row elements.
     * JS renders these from sessionStorage cart data after page load.
     */
    public java.util.List<String> getOrderItemLines() {
        var lines = new java.util.ArrayList<String>();
        try {
            var rows = page.locator("#products-div .summry_row").all();
            for (var row : rows) {
                try {
                    String name  = row.locator("#product_name").textContent().trim();
                    String price = row.locator("#product_price_actual").textContent().trim();
                    String line  = (name.isEmpty() ? row.textContent().trim() : name + "  " + price)
                                       .replaceAll("\\s+", " ");
                    if (!line.isEmpty()) lines.add(line);
                } catch (Exception ignored) {
                    String text = row.textContent().trim().replaceAll("\\s+", " ");
                    if (!text.isEmpty()) lines.add(text);
                }
            }
        } catch (Exception ignored) {}
        return lines;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Gets text of an element, returns "N/A" if missing or empty.
     */
    private String getElText(String selector) {
        try {
            String t = page.locator(selector).textContent().trim();
            return t.isEmpty() ? "N/A" : t;
        } catch (Exception e) { return "N/A"; }
    }

    /**
     * Reads the nth <li> inside a container and strips the label prefix.
     * e.g. "First Name: John" → "John"
     * Uses nth-child (1-based).
     */
    private String getLiValue(String container, int nthChild) {
        try {
            String text = page.locator(container + " li:nth-child(" + nthChild + ")")
                    .textContent().trim();
            // Strip label: "First Name: John" → "John"
            int colonIdx = text.indexOf(":");
            if (colonIdx >= 0 && colonIdx < text.length() - 1) {
                text = text.substring(colonIdx + 1).trim();
            }
            return text.isEmpty() ? "N/A" : text;
        } catch (Exception e) { return "N/A"; }
    }
}
