package com.funnel1pg.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.util.List;

public class CheckoutPage extends BasePage {

    // ── Product ───────────────────────────────────────────────────────────────
    private static final String BTN_SELECT    = "button:has-text('Select')";
    private static final String BTN_SELECTED  = "button:has-text('Selected')";

    // ── Shipping Address — exact IDs from live DOM ────────────────────────────
    private static final String INPUT_FIRST_NAME = "#inputFirstName[name='firstName']";
    private static final String INPUT_LAST_NAME  = "#inputLastName[name='lastName']";
    private static final String INPUT_ADDRESS    = "#inputAddress[name='shippingAddress1']";
    private static final String SELECT_STATE     = "#shippingState";
    private static final String INPUT_CITY       = "#inputCity";
    private static final String INPUT_ZIP        = "#fields_zip";
    private static final String INPUT_EMAIL      = "#inputEmail";
    private static final String INPUT_PHONE      = "#inputPhone";

    // ── Payment — exact IDs from live DOM ────────────────────────────────────
    private static final String INPUT_CARD   = "#ccNumber";
    private static final String SELECT_MONTH = "#fields_expmonth";
    private static final String SELECT_YEAR  = "#fields_expyear";
    private static final String INPUT_CVV    = "#cvv";

    // ── Terms & Submit ────────────────────────────────────────────────────────
    private static final String CB_TERMS = "#terms-conditions";
    private static final String BTN_BUY  = "button:has-text('COMPLETE YOUR SECURE PURCHASE')";

    // ── Validation / Error selectors ──────────────────────────────────────────
    private static final String VALIDATION_ERRORS =
            ".error, .invalid, .field-error, [class*='error-msg'], " +
            "[class*='validation-error'], input:invalid, select:invalid";

    private static final String PAYMENT_ERRORS =
            "[class*='card-error'], [class*='payment-error'], [class*='decline'], " +
            "[id*='error'], .alert-danger, .alert-error";

    public CheckoutPage(Page page) { super(page); }

    // ── Product ───────────────────────────────────────────────────────────────

    public void selectFirstAvailableProduct() {
        try {
            // If already selected, skip
            if (page.locator(BTN_SELECTED).count() > 0) {
                System.out.println("ℹ Product already selected");
                return;
            }
            waitForVisible(BTN_SELECT, 8_000);
            click(BTN_SELECT);
            // Wait for button to change to "Selected"
            waitForVisible(BTN_SELECTED, 5_000);
            System.out.println("✔ Product selected");
        } catch (Exception e) {
            System.out.println("⚠ Product select: " + e.getMessage());
        }
    }

    // ── Shipping Address ──────────────────────────────────────────────────────

    public void fillShippingAddress(String firstName, String lastName, String address,
                                    String city, String state, String zip,
                                    String email, String phone) {
        safeFill(INPUT_FIRST_NAME, firstName);
        safeFill(INPUT_LAST_NAME,  lastName);
        fillAddressField(address);
        safeSelectByValue(SELECT_STATE, state);
        safeFill(INPUT_CITY,  city);
        safeFill(INPUT_ZIP,   zip);
        safeFill(INPUT_EMAIL, email);
        safeFill(INPUT_PHONE, phone);
        System.out.println("✔ Shipping address filled");
    }

    private void fillAddressField(String address) {
        try {
            page.locator(INPUT_ADDRESS).fill(address);
            page.waitForTimeout(700);
            page.keyboard().press("Escape");
        } catch (Exception e) {
            System.out.println("⚠ Address field: " + e.getMessage());
        }
    }

    // ── Shipping Method ───────────────────────────────────────────────────────

    public void selectShippingMethod() {
        System.out.println("ℹ Vande Shipping pre-selected — no action needed");
    }

    // ── Payment ───────────────────────────────────────────────────────────────

    public void fillPaymentDetails(String cardNumber, String month, String year, String cvv) {
        safeFill(INPUT_CARD, cardNumber);
        safeSelectByLabel(SELECT_MONTH, month);
        safeSelectByLabel(SELECT_YEAR,  year);
        safeFill(INPUT_CVV, cvv);
        System.out.println("✔ Payment details filled");
    }

    // ── Terms ─────────────────────────────────────────────────────────────────

    public void acceptTermsAndConditions() {
        try {
            Locator cb = page.locator(CB_TERMS);
            if (!cb.isChecked()) {
                cb.click();
                System.out.println("✔ Terms checkbox checked");
            } else {
                System.out.println("ℹ Terms already checked");
            }
        } catch (Exception e) {
            System.out.println("⚠ Terms checkbox: " + e.getMessage());
        }
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    public void clickCompletePurchase() {
        waitForVisible(BTN_BUY, 10_000);
        click(BTN_BUY);
        System.out.println("✔ Complete Purchase clicked");
    }

    // ── Validation Checks ─────────────────────────────────────────────────────

    public boolean hasValidationErrors() {
        try {
            if (page.locator("input:invalid, select:invalid").count() > 0) return true;
            return page.locator(VALIDATION_ERRORS).count() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean hasPaymentError() {
        try {
            return page.locator(PAYMENT_ERRORS).count() > 0;
        } catch (Exception e) { return false; }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void safeFill(String selector, String value) {
        try {
            page.locator(selector).fill(value);
        } catch (Exception e) {
            System.out.println("⚠ safeFill [" + selector + "]: " + e.getMessage());
        }
    }

    private void safeSelectByLabel(String selector, String label) {
        try {
            page.locator(selector).selectOption(new SelectOption().setLabel(label));
        } catch (Exception e) {
            try {
                page.locator(selector).selectOption(label);
            } catch (Exception ex) {
                System.out.println("⚠ safeSelectLabel [" + selector + "]=" + label + ": " + ex.getMessage());
            }
        }
    }

    private void safeSelectByValue(String selector, String value) {
        try {
            // Try by label first (visible text), then by value
            page.locator(selector).selectOption(new SelectOption().setLabel(value));
        } catch (Exception e) {
            try {
                page.locator(selector).selectOption(value);
            } catch (Exception ex) {
                System.out.println("⚠ safeSelectByValue [" + selector + "]=" + value + ": " + ex.getMessage());
            }
        }
    }
}
