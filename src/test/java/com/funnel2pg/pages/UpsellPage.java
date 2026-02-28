package com.funnel2pg.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.List;

/**
 * UpsellPage - upsell/OTO page in the funnel.
 *
 * VERIFIED against live DOM of /2PageCheckout/upsell2:
 *
 *   DETECTION:
 *     <meta name="page-type" content="upsell" />
 *     URL contains /upsell
 *     form#upsell_form or form[action='place-upsell'] present
 *
 *   PRODUCT:
 *     <div class="sel-prod product product1"> inside <product> tag.
 *     JS auto-activates the product on page load.
 *     There is NO a.btn-upsell on this template.
 *     If not auto-activated, we click the card.
 *
 *   SHIPPING:
 *     <div class="shipping-option shipping-method-div payment-section">
 *     jQuery click handler sets hidden shippingId input + adds shipping-active class.
 *     Must click one before submitting. Toggling off (re-clicking active) breaks form.
 *
 *   ACCEPT / SUBMIT:
 *     <button type="submit" class="send-btn">Add to My Order</button>
 *     Inside form#upsell_form (action="place-upsell").
 *     Scoped to #upsell_form to avoid matching checkout send-btn.
 *
 *   DECLINE:
 *     <button class="decline-button" type="button">No Thanks, I'll Pass</button>
 *     Never clicked by automation.
 */
public class UpsellPage extends BasePage {

    private static final String META_PAGE_TYPE  = "meta[name='page-type']";
    private static final String PRODUCT_CARD    = ".sel-prod";
    private static final String SHIPPING_OPTION = "div.shipping-option.shipping-method-div";

    // Submit button scoped to upsell form; fallback to any send-btn
    private static final String ACCEPT_BTN      = "#upsell_form button.send-btn";
    private static final String ACCEPT_BTN_FB   = "form[action='place-upsell'] button.send-btn";
    private static final String ACCEPT_BTN_FB2  = "button.send-btn";

    private static final String UPSELL_FORM     = "form#upsell_form, form[action='place-upsell']";

    public UpsellPage(Page page) { super(page); }

    // ---- Detection ----------------------------------------------------------

    /** Returns "Name  $price" for the upsell product being offered on this page. */
    public String getUpsellProductName() {
        try {
            var prod = page.locator("product").first();
            String name  = prod.getAttribute("attr-name");
            String price = prod.getAttribute("attr-sale-price");
            if (price == null || price.equals("0") || price.isEmpty())
                price = prod.getAttribute("attr-price");
            if (name != null && !name.trim().isEmpty())
                return formatItem(name.trim(), price);
        } catch (Exception ignored) {}
        try {
            String name = page.locator(".title-block__main").first().textContent().trim();
            if (!name.isEmpty() && !name.equals("PRODUCT NAME")) return name;
        } catch (Exception ignored) {}
        try {
            return page.locator("h1, h2, h3").first().textContent().trim();
        } catch (Exception ignored) {}
        return "Upsell Product";
    }

    private String formatItem(String name, String price) {
        if (price == null || price.trim().isEmpty() || price.equals("0")) return name;
        String p = price.trim();
        return p.startsWith("$") ? name + "  " + p : name + "  $" + p;
    }

        public boolean isUpsellPage() {
        try {
            String pt = page.getAttribute(META_PAGE_TYPE, "content");
            if ("upsell".equalsIgnoreCase(pt)) return true;
        } catch (Exception ignored) {}
        try {
            if (getCurrentUrl().toLowerCase().contains("/upsell")) return true;
        } catch (Exception ignored) {}
        try {
            if (page.locator(UPSELL_FORM).count() > 0) return true;
        } catch (Exception ignored) {}
        return false;
    }

    // ---- Step 1: Product ----------------------------------------------------

    /**
     * Ensures the product card is active.
     * JS auto-activates .sel-prod on page load. We verify and click if needed.
     * No a.btn-upsell exists on this template.
     */
    public void addProductToUpsell() {
        try {
            page.waitForSelector(PRODUCT_CARD,
                    new Page.WaitForSelectorOptions()
                            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                            .setTimeout(8000));
            Locator card = page.locator(PRODUCT_CARD).first();
            String classes = card.getAttribute("class");
            if (classes != null && classes.contains("active")) {
                System.out.println("i Upsell product already active (pre-selected by JS)");
            } else {
                card.click();
                page.waitForTimeout(600);
                System.out.println("i Upsell product clicked to activate");
            }
        } catch (Exception e) {
            System.out.println("! addProductToUpsell: " + e.getMessage());
        }
    }

    // ---- Step 2: Shipping ---------------------------------------------------

    /**
     * Selects the first available shipping option if none is already active.
     * Guards against re-clicking an already-active option (jQuery toggles it off).
     */
    public void selectUpsellShipping() {
        try {
            Locator options = page.locator(SHIPPING_OPTION);
            int count = options.count();
            if (count == 0) {
                System.out.println("i No shipping options on upsell – skipping");
                return;
            }
            // Check if any already has shipping-active
            for (int i = 0; i < count; i++) {
                String cls = options.nth(i).getAttribute("class");
                if (cls != null && cls.contains("shipping-active")) {
                    System.out.println("i Upsell shipping already active – skipping click (" + count + " options)");
                    return;
                }
            }
            options.first().click();
            page.waitForTimeout(500);
            System.out.println("i Upsell shipping selected (" + count + " options available)");
        } catch (Exception e) {
            System.out.println("! selectUpsellShipping: " + e.getMessage());
        }
    }

    // ---- Step 3: Accept & Continue -----------------------------------------

    /**
     * Clicks "Add to My Order" (button.send-btn inside form#upsell_form).
     * Falls back to form[action='place-upsell'] button.send-btn then any send-btn.
     */
    public void acceptAndContinue() {
        // Capture the place-upsell response so we can diagnose any CRM errors
        final String[] crmResponse = {null};
        page.onResponse(response -> {
            try {
                if (response.url().contains("place-upsell")) {
                    String body = response.text();
                    crmResponse[0] = "HTTP " + response.status() + " " + response.url() + " -> " + body;
                }
            } catch (Exception ignored) {}
        });

        String[] selectors = { ACCEPT_BTN, ACCEPT_BTN_FB, ACCEPT_BTN_FB2 };
        for (String sel : selectors) {
            try {
                Locator btn = page.locator(sel).first();
                if (btn.count() > 0 && btn.isVisible()) {
                    String label = btn.textContent().trim().replaceAll("\\s+", " ");
                    btn.scrollIntoViewIfNeeded();
                    btn.click();
                    // Wait briefly for the AJAX call to fire and respond
                    page.waitForTimeout(1500);
                    if (crmResponse[0] != null) {
                        System.out.println("  CRM Response: " + crmResponse[0]);
                    }
                    System.out.println("i Upsell accepted via [" + sel + "] label='" + label + "'");
                    return;
                }
            } catch (Exception ignored) {}
        }
        // Diagnostic: log all visible buttons
        System.out.println("! No accept button matched. Visible buttons on page:");
        try {
            for (Locator b : page.locator("button").all()) {
                try {
                    if (b.isVisible())
                        System.out.println("  button class=[" + b.getAttribute("class") + "] text='" + b.textContent().trim() + "'");
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    // ---- Helpers ------------------------------------------------------------

    public List<String> getAvailableShippingMethods() {
        try {
            List<String> result = new java.util.ArrayList<>();
            for (Locator opt : page.locator(SHIPPING_OPTION + " .shipping-title").all())
                try { result.add(opt.textContent().trim()); } catch (Exception ignored) {}
            return result;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
