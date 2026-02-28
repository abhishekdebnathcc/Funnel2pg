package com.funnel2pg.pages;

import com.funnel2pg.config.ConfigReader;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * CheckoutPage - handles Step 2 (payment form) of the 2pgCCF25Feb funnel.
 *
 * PAYMENT METHOD LOGIC:
 *   The CRM configures which payment tiles shown via JS at runtime.
 *   Tiles NOT configured stay hidden (class d-none).
 *
 *   Case A - Single payment method (credit card only):
 *     All tiles remain d-none. #credit-card-fields is ALREADY visible.
 *     -> Skip tile click entirely, fill card fields directly.
 *
 *   Case B - Multiple payment methods configured:
 *     At least one tile has d-none removed by JS (tile is visible).
 *     -> Must click the credit card tile to reveal #credit-card-fields.
 *
 *   Detection: count [data-payment] elements that lack d-none in #payment-methods-wrapper.
 */
public class CheckoutPage extends BasePage {

    // Product
    private static final String BTN_SELECT_PRODUCT = ".sel-prod";

    // Payment tiles (inside #payment-methods-wrapper)
    private static final String PM_CREDIT_TILE = "[data-payment='creditcard'] .payment-method-text";
    private static final String PM_COD_TILE    = "[data-payment='cash-on-delivery'] .payment-method-text";

    // Credit card fields (#credit-card-fields)
    private static final String INPUT_CARD   = "[name='creditCardNumber']";
    private static final String SELECT_MONTH = "#expmonth";
    private static final String SELECT_YEAR  = "#expyear";
    private static final String INPUT_CVV    = "[name='CVV']";

    // Terms & Submit
    private static final String CB_AGREE = "#agree-checkbox";
    private static final String BTN_BUY  = "button.send-btn";

    // Errors
    private static final String FORM_ERROR        = "#formError";
    private static final String VALIDATION_ERRORS = ".error, .invalid, input:invalid, select:invalid, [class*='error-msg'], [class*='validation-error']";
    private static final String PAYMENT_ERRORS    = "#formError, [class*='card-error'], [class*='payment-error'], [class*='decline'], .alert-danger";

    public CheckoutPage(Page page) { super(page); }

    // ---- Product ------------------------------------------------------------

    public void selectFirstAvailableProduct() {
        if (ConfigReader.isSelectAllProducts()) {
            selectAllProducts();
            return;
        }
        try {
            page.waitForSelector(BTN_SELECT_PRODUCT,
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(10_000));
            page.locator(BTN_SELECT_PRODUCT).first().click();
            page.waitForTimeout(500);
            System.out.println("✓ Product selected");
        } catch (Exception e) {
            System.out.println("✗ Product select: " + e.getMessage());
        }
    }

    /**
     * Selects ALL available products (every .sel-prod button).
     * Activated when -Dselect.all.products=true.
     */
    public void selectAllProducts() {
        try {
            page.waitForSelector(BTN_SELECT_PRODUCT,
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(10_000));
            var btns = page.locator(BTN_SELECT_PRODUCT).all();
            System.out.println("i Selecting all " + btns.size() + " product(s)");
            for (var btn : btns) {
                try { btn.click(); page.waitForTimeout(300); } catch (Exception ignored) {}
            }
            System.out.println("✓ All products selected");
        } catch (Exception e) {
            System.out.println("✗ selectAllProducts: " + e.getMessage());
        }
    }

    /**
     * Selects all visible cross-sell products (crosssellproduct elements).
     * Click target is the inner .product_box div — this toggles .active
     * and updates the .btn-add UI via jQuery in custom.js.
     * Skips any that have the .cross-disabled class.
     * Returns the count added.
     */
    public int selectAllCrossSellProducts() {
        int added = 0;
        try {
            // Wait briefly for JS to render cross-sell products from attr-* tags
            page.waitForTimeout(1000);
            var crossSells = page.locator("crosssellproduct").all();
            if (crossSells.isEmpty()) {
                System.out.println("i No cross-sell products found");
                return 0;
            }
            System.out.println("i Found " + crossSells.size() + " cross-sell product(s)");
            for (var cs : crossSells) {
                try {
                    // Skip if disabled
                    var box = cs.locator(".product_box");
                    String cls = box.getAttribute("class");
                    if (cls != null && cls.contains("cross-disabled")) {
                        System.out.println("  ⊘ Cross-sell disabled – skipping");
                        continue;
                    }
                    // Already active? Skip (toggling would de-select)
                    if (cls != null && cls.contains("active")) {
                        System.out.println("  i Cross-sell already active");
                        added++;
                        continue;
                    }
                    String name = "";
                    try { name = cs.getAttribute("attr-name"); } catch (Exception ignored) {}
                    box.click();
                    page.waitForTimeout(400);
                    added++;
                    System.out.println("  ✓ Cross-sell added: " + (name == null || name.isEmpty() ? "(unnamed)" : name));
                } catch (Exception e) {
                    System.out.println("  ✗ Could not add cross-sell: " + e.getMessage());
                }
            }
            if (added > 0) System.out.println("✓ Cross-sell products selected: " + added);
        } catch (Exception e) {
            System.out.println("✗ Cross-sell selection error: " + e.getMessage());
        }
        return added;
    }

    /**
     * Returns names of main products from <product attr-name="..."> custom elements.
     * Called immediately after selection while still on the checkout page.
     */
    public java.util.List<String> getSelectedMainProductNames() {
        var names = new java.util.ArrayList<String>();
        try {
            var products = page.locator("product").all();
            for (var p : products) {
                try {
                    String name = p.getAttribute("attr-name");
                    if (name != null && !name.trim().isEmpty()) {
                        String sale  = p.getAttribute("attr-sale-price");
                        String price = p.getAttribute("attr-price");
                        String eff   = (sale != null && !sale.isEmpty()
                                        && Double.parseDouble(sale) > 0) ? sale : price;
                        names.add(name.trim() + (eff != null && !eff.isEmpty() ? "  $" + eff : ""));
                        continue;
                    }
                } catch (Exception ignored) {}
                try {
                    String name = p.locator(".title-block__main").textContent().trim();
                    names.add(name.isEmpty() ? "Main Product" : name);
                } catch (Exception ignored) { names.add("Main Product"); }
            }
        } catch (Exception ignored) {}
        if (names.isEmpty()) names.add("Main Product");
        return names;
    }

    /**
     * Returns names of selected cross-sell products (those with .active on .product_box).
     * Reads attr-name from the parent <crosssellproduct> element.
     * Must be called while still on the checkout page.
     */
    public java.util.List<String> getSelectedCrossSellNames() {
        var names = new java.util.ArrayList<String>();
        try {
            var active = page.locator("crosssellproduct .product_box.active").all();
            for (var box : active) {
                try {
                    var parent = box.locator("xpath=..");
                    String name  = parent.getAttribute("attr-name");
                    if (name == null || name.trim().isEmpty())
                        name = box.locator(".product_title").textContent().trim();
                    if (name == null || name.trim().isEmpty()) name = "Cross-sell Product";
                    String sale  = parent.getAttribute("attr-sale-price");
                    String price = parent.getAttribute("attr-price");
                    String eff   = (sale != null && !sale.isEmpty()
                                    && Double.parseDouble(sale) > 0) ? sale : price;
                    names.add(name.trim() + (eff != null && !eff.isEmpty() ? "  $" + eff : ""));
                } catch (Exception ignored) { names.add("Cross-sell Product"); }
            }
        } catch (Exception ignored) {}
        return names;
    }

    // ---- Shipping -----------------------------------------------------------

    public void selectShippingMethod() {
        try {
            Locator options = page.locator(".shipping-option");
            if (options.count() > 0) {
                options.first().click();
                page.waitForTimeout(400);
                System.out.println("✓ Shipping method selected");
            } else {
                System.out.println("i Shipping: no options found - may be pre-selected");
            }
        } catch (Exception e) {
            System.out.println("✗ Shipping select: " + e.getMessage());
        }
    }

    // ---- Payment ------------------------------------------------------------

    /**
     * Smart credit card fill:
     *   - If only one payment method is configured (all tiles d-none):
     *       CC fields are already visible -> fill directly, no tile click.
     *   - If multiple payment methods (at least one tile visible):
     *       Click the credit card tile first, wait for CC fields to appear, then fill.
     */
    public void selectAndFillCreditCard(String cardNumber, String month, String year, String cvv) {
        // Brief pause for JS to finish rendering tiles after page load
        page.waitForTimeout(800);

        if (hasVisiblePaymentTiles()) {
            System.out.println("i Multiple payment methods detected - clicking credit card tile");
            clickPaymentTile(PM_CREDIT_TILE, "Credit Card");
            // Wait for CC fields to become visible
            try {
                page.waitForFunction(
                        "!document.getElementById('credit-card-fields').classList.contains('d-none')",
                        null,
                        new Page.WaitForFunctionOptions().setTimeout(5000));
            } catch (Exception ignored) {}
        } else {
            System.out.println("i Single payment method - CC fields already visible, filling directly");
        }

        safeFill(INPUT_CARD, cardNumber);
        safeSelectByLabel(SELECT_MONTH, month);
        safeSelectByLabel(SELECT_YEAR, year);
        safeFill(INPUT_CVV, cvv);
        System.out.println("✓ Credit card details filled");
    }

    public void selectCashOnDelivery() {
        page.waitForTimeout(800);
        if (hasVisiblePaymentTiles()) {
            clickPaymentTile(PM_COD_TILE, "Cash on Delivery");
        } else {
            System.out.println("i Single payment method configured - no CoD tile to click");
        }
        System.out.println("✓ Cash on Delivery selected");
    }

    /**
     * Returns true if at least one [data-payment] tile inside
     * #payment-methods-wrapper does NOT have the d-none class.
     * If all tiles are d-none the funnel has only one payment method
     * and shows CC fields directly without a tile chooser.
     */
    private boolean hasVisiblePaymentTiles() {
        try {
            long visibleCount = ((Number) page.evaluate(
                    "(function() {" +
                    "  var tiles = document.querySelectorAll('#payment-methods-wrapper [data-payment]');" +
                    "  var n = 0;" +
                    "  tiles.forEach(function(t) { if (!t.classList.contains('d-none')) n++; });" +
                    "  return n;" +
                    "})()"
            )).longValue();
            System.out.println("i Visible payment tiles: " + visibleCount);
            return visibleCount > 0;
        } catch (Exception e) {
            System.out.println("! Could not detect payment tiles: " + e.getMessage());
            return false; // safe default: treat as single method, fill directly
        }
    }

    private void clickPaymentTile(String selector, String label) {
        try {
            Locator tile = page.locator(selector);
            tile.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000));
            tile.click();
            page.waitForTimeout(400);
            System.out.println("✓ Payment tile clicked: " + label);
        } catch (Exception e) {
            System.out.println("! Tile click failed [" + label + "], trying force: " + e.getMessage());
            try {
                page.locator(selector).click(new Locator.ClickOptions().setForce(true));
                page.waitForTimeout(400);
            } catch (Exception ex) {
                System.out.println("✗ Could not click tile [" + label + "]: " + ex.getMessage());
            }
        }
    }

    // ---- Terms & Submit -----------------------------------------------------

    public void acceptTermsAndConditions() {
        try {
            Locator cb = page.locator(CB_AGREE);
            if (cb.count() > 0 && !cb.isChecked()) {
                cb.click();
                System.out.println("✓ Agree checkbox checked");
            } else {
                System.out.println("i Agree checkbox not found or already checked");
            }
        } catch (Exception e) {
            System.out.println("✗ Agree checkbox: " + e.getMessage());
        }
    }

    public void clickCompletePurchase() {
        waitForVisible(BTN_BUY, 10_000);
        click(BTN_BUY);
        System.out.println("✓ Complete Purchase clicked");
    }

    // ---- Validation ---------------------------------------------------------

    public boolean hasValidationErrors() {
        try {
            return page.locator(VALIDATION_ERRORS).count() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean hasPaymentError() {
        try {
            Locator err = page.locator(FORM_ERROR);
            if (err.count() > 0 && err.isVisible()) return true;
            return page.locator(PAYMENT_ERRORS).count() > 0;
        } catch (Exception e) { return false; }
    }

    // ---- Helpers ------------------------------------------------------------

    private void safeFill(String selector, String value) {
        try {
            page.locator(selector).fill(value);
        } catch (Exception e) {
            System.out.println("✗ safeFill [" + selector + "]: " + e.getMessage());
        }
    }

    private void safeSelectByLabel(String selector, String label) {
        try {
            page.locator(selector).selectOption(new SelectOption().setLabel(label));
        } catch (Exception e) {
            try {
                page.locator(selector).selectOption(label);
            } catch (Exception ex) {
                System.out.println("✗ safeSelect [" + selector + "]=" + label + ": " + ex.getMessage());
            }
        }
    }
}
