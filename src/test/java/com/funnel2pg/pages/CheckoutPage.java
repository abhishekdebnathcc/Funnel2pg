package com.funnel2pg.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

/**
 * CheckoutPage – handles Step 2 (payment form) of the 2pgCCF25Feb funnel.
 *
 * LAYOUT (from DOM inspection of /2pgCCF25Feb/checkout):
 *   - Left column: product selection, shipping, related product
 *   - Right column: payment form  (form#checkout_form action="place-order")
 *
 * PRODUCT SELECTION:
 *   - Products render as .sel-prod boxes; clicking selects them.
 *   - Hidden input: #productId name="products[0][productId]"
 *
 * BILLING:
 *   - radio#radio-yes  → billing same as shipping (default)
 *   - radio#radio-no   → show billing fields (#billing-block)
 *
 * PAYMENT METHODS (tiles in #payment-methods-wrapper):
 *   - [data-payment='creditcard']        .payment-method-text
 *   - [data-payment='cash-on-delivery']  .payment-method-text
 *   - [data-payment='paypal']            .payment-method-text
 *
 * CREDIT CARD FIELDS (#credit-card-fields, hidden until tile clicked):
 *   - name="creditCardNumber"
 *   - #expmonth, #expyear
 *   - name="CVV"
 *
 * SUBMIT:
 *   - button.send-btn  (has data-triggeraction="accept_continue")
 */
public class CheckoutPage extends BasePage {

    // ── Product ───────────────────────────────────────────────────────────────
    // Products are rendered as .sel-prod elements; first visible one is clicked
    private static final String BTN_SELECT_PRODUCT = ".sel-prod";

    // ── Related/Cross product ─────────────────────────────────────────────────
    private static final String RELATED_PRODUCT_SECTION = ".related_product_wrapper";

    // ── Billing ───────────────────────────────────────────────────────────────
    private static final String RADIO_BILLING_SAME = "#radio-yes";

    // ── Payment method tiles ──────────────────────────────────────────────────
    private static final String PM_CREDIT_TILE = "[data-payment='creditcard'] .payment-method-text";
    private static final String PM_COD_TILE    = "[data-payment='cash-on-delivery'] .payment-method-text";

    // ── Credit card fields ────────────────────────────────────────────────────
    private static final String CC_FIELDS_DIV  = "#credit-card-fields";
    private static final String INPUT_CARD     = "[name='creditCardNumber']";
    private static final String SELECT_MONTH   = "#expmonth";
    private static final String SELECT_YEAR    = "#expyear";
    private static final String INPUT_CVV      = "[name='CVV']";

    // ── Terms & Submit ────────────────────────────────────────────────────────
    private static final String CB_AGREE  = "#agree-checkbox";
    private static final String BTN_BUY   = "button.send-btn";

    // ── Validation / error ────────────────────────────────────────────────────
    private static final String FORM_ERROR    = "#formError";
    private static final String PRODUCT_ERROR = "#productError";
    private static final String VALIDATION_ERRORS =
            ".error, .invalid, input:invalid, select:invalid, " +
            "[class*='error-msg'], [class*='validation-error']";
    private static final String PAYMENT_ERRORS =
            "#formError:visible, [class*='card-error'], [class*='payment-error'], " +
            "[class*='decline'], .alert-danger";

    public CheckoutPage(Page page) { super(page); }

    // ── Product ───────────────────────────────────────────────────────────────

    /**
     * Selects the first available product on the checkout page.
     * Products are rendered by JS as .sel-prod boxes once page loads.
     */
    public void selectFirstAvailableProduct() {
        try {
            // Wait for product boxes to appear
            page.waitForSelector(BTN_SELECT_PRODUCT,
                    new Page.WaitForSelectorOptions()
                            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                            .setTimeout(10_000));
            page.locator(BTN_SELECT_PRODUCT).first().click();
            page.waitForTimeout(500);
            System.out.println("✓ Product selected");
        } catch (Exception e) {
            System.out.println("✗ Product select: " + e.getMessage());
        }
    }

    // ── Shipping ──────────────────────────────────────────────────────────────

    /**
     * Selects the first visible shipping option.
     * Shipping options are rendered as .shipping-option divs.
     */
    public void selectShippingMethod() {
        try {
            Locator options = page.locator(".shipping-option");
            if (options.count() > 0) {
                options.first().click();
                page.waitForTimeout(400);
                System.out.println("✓ Shipping method selected");
            } else {
                System.out.println("ℹ No shipping options found – may be pre-selected");
            }
        } catch (Exception e) {
            System.out.println("✗ Shipping select: " + e.getMessage());
        }
    }

    // ── Payment ───────────────────────────────────────────────────────────────

    /**
     * Selects Credit Card payment tile and fills card details.
     */
    public void selectAndFillCreditCard(String cardNumber, String month, String year, String cvv) {
        clickPaymentTile(PM_CREDIT_TILE, "Credit Card");
        // Wait for CC fields to become visible
        try {
            page.waitForFunction(
                    "!document.getElementById('credit-card-fields').classList.contains('d-none')",
                    null,
                    new Page.WaitForFunctionOptions().setTimeout(5000)
            );
        } catch (Exception ignored) {}
        safeFill(INPUT_CARD, cardNumber);
        safeSelectByLabel(SELECT_MONTH, month);
        safeSelectByLabel(SELECT_YEAR, year);
        safeFill(INPUT_CVV, cvv);
        System.out.println("✓ Credit card details filled");
    }

    /**
     * Selects Cash on Delivery payment tile.
     */
    public void selectCashOnDelivery() {
        clickPaymentTile(PM_COD_TILE, "Cash on Delivery");
        System.out.println("✓ Cash on Delivery selected");
    }

    private void clickPaymentTile(String selector, String label) {
        try {
            page.waitForTimeout(1500);
            Locator tile = page.locator(selector);
            tile.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(8000));
            tile.click();
            page.waitForTimeout(500);
            System.out.println("✓ Payment tile clicked: " + label);
        } catch (Exception e) {
            System.out.println("⚠ Normal click failed for [" + label + "], trying force click");
            try {
                page.locator(selector).click(new Locator.ClickOptions().setForce(true));
                page.waitForTimeout(500);
            } catch (Exception ex) {
                System.out.println("✗ Could not click payment tile [" + label + "]: " + ex.getMessage());
            }
        }
    }

    // ── Terms & Submit ────────────────────────────────────────────────────────

    /**
     * Checks the agree-checkbox if present and not already checked.
     */
    public void acceptTermsAndConditions() {
        try {
            Locator cb = page.locator(CB_AGREE);
            if (cb.count() > 0 && !cb.isChecked()) {
                cb.click();
                System.out.println("✓ Agree checkbox checked");
            } else {
                System.out.println("ℹ Agree checkbox not found or already checked");
            }
        } catch (Exception e) {
            System.out.println("✗ Agree checkbox: " + e.getMessage());
        }
    }

    public void clickCompletePurchase() {
        waitForVisible(BTN_BUY, 10_000);
        click(BTN_BUY);
        System.out.println("✓ Complete Purchase (send-btn) clicked");
    }

    // ── Validation ────────────────────────────────────────────────────────────

    public boolean hasValidationErrors() {
        try {
            return page.locator(VALIDATION_ERRORS).count() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean hasPaymentError() {
        try {
            // Check visible #formError
            Locator err = page.locator(FORM_ERROR);
            if (err.count() > 0 && err.isVisible()) return true;
            return page.locator(PAYMENT_ERRORS).count() > 0;
        } catch (Exception e) { return false; }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
                System.out.println("✗ safeSelect [" + selector + "]='" + label + "': " + ex.getMessage());
            }
        }
    }
}
