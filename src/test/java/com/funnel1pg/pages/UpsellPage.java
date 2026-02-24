package com.funnel1pg.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.function.Predicate;

public class UpsellPage extends BasePage {

    private static final String ACCEPT_BTNS =
            "button:has-text('YES'), a:has-text('YES'), " +
            "button:has-text('Add to'), a:has-text('Add to'), " +
            "button:has-text('Upgrade'), a:has-text('Upgrade'), " +
            "[class*='accept']";

    private static final String DECLINE_BTNS =
            "button:has-text('NO'), a:has-text('NO'), " +
            "button:has-text('No thanks'), a:has-text('No thanks'), " +
            "button:has-text('Skip'), a:has-text('Skip'), " +
            "a:has-text('No, I'), " +
            "[class*='decline']";

    private static final String THANKYOU_HEADING =
            "h1:has-text('Thank'), h2:has-text('Thank'), " +
            "h1:has-text('Order Confirmed'), h2:has-text('Order Confirmed')";

    public UpsellPage(Page page) { 
        super(page); 
    }

    // ===== PAGE DETECTION =========================================================

    /**
     * Detect if current page is an upsell page
     */
    public boolean isUpsellPage() {
        String url = getCurrentUrl().toLowerCase();
        if (url.contains("upsell") || url.contains("oto") ||
            url.contains("offer")  || url.contains("upgrade")) return true;
        try {
            List<Locator> btns = page.locator(DECLINE_BTNS + ", " + ACCEPT_BTNS).all();
            return !btns.isEmpty() && btns.get(0).isVisible();
        } catch (Exception e) { 
            return false; 
        }
    }

    /**
     * Detect if current page is thank you/confirmation page
     */
    public boolean isThankYouPage() {
        String url = getCurrentUrl().toLowerCase();
        if (url.contains("thank") || url.contains("confirm") ||
            url.contains("success") || url.contains("receipt")) return true;
        try {
            return page.locator(THANKYOU_HEADING).count() > 0;
        } catch (Exception e) { 
            return false; 
        }
    }

    // ===== UPSELL PRODUCT SELECTION ===============================================

    /**
     * Add the upsell product to the order
     * Handles various button text patterns
     */
    public void addProductToUpsell() {
        try {
            // Look for product selection - could be radio button or checkbox
            var productSelectors = new String[] {
                    "input[type='radio'][name*='product'], " +
                    "input[type='checkbox'][name*='product'], " +
                    "[class*='product-option'] input",
                    "label:has-text('Product')"
            };
            
            for (String selector : productSelectors) {
                try {
                    var element = page.locator(selector).first();
                    if (element.isVisible()) {
                        element.click();
                        System.out.println("✓ Upsell product selected: " + selector);
                        page.waitForTimeout(500);
                        return;
                    }
                } catch (Exception e) {
                    // Continue to next selector
                }
            }
            
            System.out.println("ℹ No explicit product selection found (auto-selected)");
        } catch (Exception e) {
            System.out.println("⚠ Error selecting upsell product: " + e.getMessage());
        }
    }

    /**
     * Get the upsell product name/description
     */
    public String getUpsellProductName() {
        try {
            // Look for product title/name on upsell page
            var productNameSelectors = new String[] {
                    "[class*='product-name'], [class*='offer-title'], " +
                    "[class*='upsell-title']",
                    "h2, h3",
                    "span:has-text('Product')"
            };
            
            for (String selector : productNameSelectors) {
                try {
                    String text = page.locator(selector).first().textContent().trim();
                    if (!text.isEmpty() && text.length() < 200) {
                        return text;
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
            return "Unknown Product";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get upsell product price
     */
    public String getUpsellPrice() {
        try {
            // Look for price on upsell page
            var priceSelectors = new String[] {
                    "[class*='price'], [class*='amount'], [class*='cost']",
                    "span:has-text('$')",
                    "div:has-text('$')"
            };
            
            for (String selector : priceSelectors) {
                try {
                    String text = page.locator(selector).first().textContent().trim();
                    if (text.matches(".*\\$[0-9]+\\.?[0-9]*.*")) {
                        return text;
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
            return "Not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ===== UPSELL SHIPPING SELECTION ==============================================

    /**
     * Select shipping method for upsell
     */
    public void selectUpsellShipping() {
        try {
            // Try to find shipping method options (radio buttons)
            var shippingOptions = page.locator(
                    "input[type='radio'][name*='ship'], " +
                    "input[type='radio'][name*='method'], " +
                    "[class*='shipping-option']"
            ).all();
            
            if (!shippingOptions.isEmpty()) {
                // Select the first available shipping option
                shippingOptions.get(0).click();
                System.out.println("✓ Upsell shipping method selected");
                page.waitForTimeout(500);
            } else {
                System.out.println("ℹ No shipping selection available for upsell");
            }
        } catch (Exception e) {
            System.out.println("⚠ Error selecting upsell shipping: " + e.getMessage());
        }
    }

    /**
     * Get available shipping methods
     */
    public List<String> getAvailableShippingMethods() {
        try {
            var shippingLabels = page.locator(
                    "label:has(input[type='radio'][name*='ship']), " +
                    "label:has(input[type='radio'][name*='method'])"
            ).all();
            
            var methods = new java.util.ArrayList<String>();
            for (var label : shippingLabels) {
                methods.add(label.textContent().trim());
            }
            return methods;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    // ===== UPSELL ACCEPTANCE & CONTINUATION =======================================

    /**
     * Accept the upsell offer and continue
     * (Primary method - used in main flow)
     */
    public void acceptAndContinue() {
        try {
            // Look for continue/accept button
            var continueBtn = page.locator(
                    "button:has-text('Continue'), button:has-text('Accept'), " +
                    "button:has-text('YES'), button:has-text('Proceed'), " +
                    "a:has-text('Continue'), a:has-text('Accept'), " +
                    "a:has-text('YES'), a:has-text('Proceed'), " +
                    "[class*='continue'], [class*='submit'], " +
                    "[role='button']:has-text('Continue')"
            ).first();
            
            continueBtn.click();
            System.out.println("✓ Upsell offer accepted and continued");
            page.waitForTimeout(1000);
        } catch (Exception e) {
            System.out.println("⚠ Error accepting upsell: " + e.getMessage());
        }
    }

    /**
     * Decline the upsell offer
     * (Legacy method - kept for backward compatibility)
     */
    public void declineOffer() {
        try {
            page.locator(DECLINE_BTNS).first().click();
            System.out.println("✓ Declined upsell on: " + getCurrentUrl());
        } catch (Exception e) {
            System.out.println("⚠ No decline btn — trying accept: " + getCurrentUrl());
            try {
                page.locator(ACCEPT_BTNS).first().click();
                System.out.println("✓ Accepted (fallback) on: " + getCurrentUrl());
            } catch (Exception ex) {
                System.out.println("⚠ No upsell buttons found: " + ex.getMessage());
            }
        }
    }

    // ===== LEGACY NAVIGATION METHOD (for backward compatibility) ====================

    /**
     * Navigate through all upsells by declining them
     * (Legacy method - kept for backward compatibility)
     */
    public void navigateThroughAllUpsells() {
        int max = 5, count = 0;
        while (count < max) {
            page.waitForLoadState();
            System.out.println("→ [" + count + "] URL: " + getCurrentUrl());

            if (isThankYouPage()) {
                System.out.println("✓ Thank-you page reached after " + count + " upsell(s)");
                break;
            }
            if (!isUpsellPage()) {
                System.out.println("ℹ No upsell detected — exiting loop");
                break;
            }

            String before = getCurrentUrl();
            declineOffer();

            Predicate<String> urlChanged = url -> !url.equals(before);
            try {
                page.waitForURL(urlChanged,
                        new Page.WaitForURLOptions().setTimeout(10_000));
            } catch (Exception e) {
                page.waitForTimeout(3000);
            }
            count++;
        }
        System.out.println("✓ Upsell done. Final URL: " + getCurrentUrl());
    }
}