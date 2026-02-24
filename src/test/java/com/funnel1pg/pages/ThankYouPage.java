package com.funnel1pg.pages;

import com.microsoft.playwright.Page;

public class ThankYouPage extends BasePage {

    private static final String HEADING_SELECTOR =
            "h1:has-text('Thank'), h2:has-text('Thank'), " +
            "h1:has-text('Order Confirmed'), h2:has-text('Order Confirmed'), " +
            "h1:has-text('Success'), h2:has-text('Success'), " +
            ".thank-you-title, [class*='thank']";

    public ThankYouPage(Page page) { super(page); }

    // ===== PAGE DETECTION =====
    
    public boolean isThankYouPageDisplayed() {
        String url = getCurrentUrl().toLowerCase();
        if (url.contains("thank") || url.contains("confirm") ||
            url.contains("success") || url.contains("receipt")) return true;
        try {
            return page.locator(HEADING_SELECTOR).count() > 0;
        } catch (Exception e) { return false; }
    }

    // ===== PAGE CONTENT EXTRACTION =====
    
    public String getHeading() {
        try {
            return page.locator("h1, h2").first().textContent().trim();
        } catch (Exception e) { return "(no heading found)"; }
    }

    public String getOrderConfirmationText() {
        try {
            return page.locator("body").textContent()
                    .replaceAll("\\s+", " ").trim().substring(0, 300);
        } catch (Exception e) { return "(body not available)"; }
    }

    // ===== ORDER DETAIL EXTRACTION =====
    
    /**
     * Extract order number from thank you page
     * Looks for common patterns: "Order #123456", "Order Number:", etc.
     */
    public String getOrderNumber() {
        try {
            // Try common selectors for order number
            var selectors = new String[] {
                    "span:has-text('Order'), [class*='order-number'], " +
                    "[class*='orderId'], [id*='order-num']",
                    "p:has-text('Order')",
                    "div:has-text('Order #')"
            };
            
            for (String selector : selectors) {
                try {
                    String text = page.locator(selector).first().textContent();
                    // Extract number pattern (e.g., "123456" from "Order #123456")
                    String number = text.replaceAll("[^0-9]", "");
                    if (!number.isEmpty()) return number;
                } catch (Exception e) {
                    // Continue to next selector
                }
            }
            
            return "Not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Extract order price/total from thank you page
     * Looks for: "$X.XX", "Total: $X.XX", etc.
     */
    public String getOrderPrice() {
        try {
            // Try to find price/total selectors
            var selectors = new String[] {
                    "[class*='total'], [class*='price'], [class*='amount']",
                    "span:has-text('$')",
                    "div:has-text('Total')"
            };
            
            for (String selector : selectors) {
                try {
                    String text = page.locator(selector).first().textContent();
                    // Extract currency pattern (e.g., "$123.45")
                    if (text.matches(".*\\$[0-9]+\\.?[0-9]*.*")) {
                        return text.trim();
                    }
                } catch (Exception e) {
                    // Continue to next selector
                }
            }
            
            return "Not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Extract shipping address from thank you page
     */
    public String getShippingAddress() {
        try {
            // Try common selectors for shipping address
            var selectors = new String[] {
                    "[class*='shipping-address'], [class*='ship-addr'], " +
                    "[id*='ship-address']",
                    "div:has-text('Address')",
                    "p:has-text('Street')"
            };
            
            for (String selector : selectors) {
                try {
                    String text = page.locator(selector).first().textContent();
                    if (!text.trim().isEmpty()) return text.trim();
                } catch (Exception e) {
                    // Continue to next selector
                }
            }
            
            // Fallback: get any text containing numbers/street patterns
            String bodyText = page.locator("body").textContent();
            if (bodyText.contains("Street") || bodyText.contains("Ave") || 
                bodyText.contains("Road") || bodyText.contains("Dr")) {
                return bodyText.substring(0, Math.min(200, bodyText.length()));
            }
            
            return "Not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Extract order items/products from thank you page
     */
    public String getOrderItems() {
        try {
            // Try to find items list
            var itemElements = page.locator("[class*='order-item'], " +
                    "[class*='product'], [class*='line-item'], " +
                    "li:has-text('Product')").all();
            
            if (!itemElements.isEmpty()) {
                StringBuilder items = new StringBuilder();
                for (int i = 0; i < Math.min(5, itemElements.size()); i++) {
                    String itemText = itemElements.get(i).textContent().trim();
                    if (!itemText.isEmpty()) {
                        items.append(itemText).append("; ");
                    }
                }
                return items.toString();
            }
            
            return "Not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get full order summary text
     */
    public String getOrderSummary() {
        try {
            // Try to find summary section
            var selectors = new String[] {
                    "[class*='order-summary']",
                    "[class*='confirmation']",
                    "section:has-text('Order')"
            };
            
            for (String selector : selectors) {
                try {
                    String text = page.locator(selector).first().textContent();
                    if (!text.isEmpty()) return text.trim();
                } catch (Exception e) {
                    // Continue
                }
            }
            
            return "Not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}