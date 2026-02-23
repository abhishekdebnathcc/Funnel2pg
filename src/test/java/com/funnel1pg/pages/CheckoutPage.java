package com.funnel1pg.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

import java.util.List;

public class CheckoutPage extends BasePage {

    // ── Product ───────────────────────────────────────────────────────────────
    private static final String BTN_SELECT = "button:has-text('Select')";

    // ── Shipping Address ──────────────────────────────────────────────────────
    private static final String INPUT_FIRST_NAME = "input[placeholder='First Name']";
    private static final String INPUT_LAST_NAME  = "input[placeholder='Last Name']";
    private static final String INPUT_ADDRESS    = "input[placeholder='Enter a location']";
    private static final String SELECT_STATE     = "select:has(option:text-is('Select State'))";
    private static final String INPUT_CITY       = "input[placeholder='City']";
    private static final String INPUT_ZIP        = "input[placeholder='Zip Code:']";
    private static final String INPUT_EMAIL      = "input[placeholder='Email address']";
    private static final String INPUT_PHONE      = "input[placeholder='Phone']";

    // ── Payment ───────────────────────────────────────────────────────────────
    private static final String INPUT_CARD   = "input[placeholder='Card Number']";
    private static final String SELECT_MONTH = "select:has(option:text-is('Month'))";
    private static final String SELECT_YEAR  = "select:has(option:text-is('Year'))";
    private static final String INPUT_CVV    = "input[placeholder='Security code']";

    // ── Terms & Submit ────────────────────────────────────────────────────────
    private static final String BTN_BUY = "button:has-text('COMPLETE YOUR SECURE PURCHASE')";

    // ── Validation selectors ──────────────────────────────────────────────────
    // Common error patterns: red borders, error messages, required field hints
    private static final String VALIDATION_ERRORS =
            ".error, .invalid, .field-error, [class*='error'], " +
            "[class*='invalid'], .form-error, " +
            "input:invalid, select:invalid, " +
            ":text('required'), :text('Required'), " +
            ":text('Please enter'), :text('cannot be blank')";

    private static final String PAYMENT_ERRORS =
            ":text('invalid card'), :text('Invalid card'), :text('card number'), " +
            ":text('Card Number'), :text('declined'), :text('Declined'), " +
            ":text('payment'), :text('Payment'), [class*='card-error'], " +
            ".payment-error, [class*='payment-error']";

    public CheckoutPage(Page page) { super(page); }

    // ── Product ───────────────────────────────────────────────────────────────

    public void selectFirstAvailableProduct() {
        try {
            waitForVisible(BTN_SELECT);
            click(BTN_SELECT);
        } catch (Exception e) {
            System.out.println("ℹ No Select button — product may be pre-selected");
        }
    }

    // ── Shipping Address ──────────────────────────────────────────────────────

    public void fillShippingAddress(String firstName, String lastName, String address,
                                    String city, String state, String zip,
                                    String email, String phone) {
        safeFill(INPUT_FIRST_NAME, firstName);
        safeFill(INPUT_LAST_NAME,  lastName);
        fillAddressField(address);
        safeSelectLabel(SELECT_STATE, state);
        safeFill(INPUT_CITY,  city);
        safeFill(INPUT_ZIP,   zip);
        safeFill(INPUT_EMAIL, email);
        safeFill(INPUT_PHONE, phone);
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
        // Vande Shipping is the only option and pre-selected — nothing to do
        System.out.println("ℹ Shipping: Vande Shipping pre-selected");
    }

    // ── Payment ───────────────────────────────────────────────────────────────

    public void fillPaymentDetails(String cardNumber, String month, String year, String cvv) {
        safeFill(INPUT_CARD, cardNumber);
        safeSelectLabel(SELECT_MONTH, month);
        safeSelectLabel(SELECT_YEAR,  year);
        safeFill(INPUT_CVV, cvv);
    }

    // ── Terms ─────────────────────────────────────────────────────────────────

    public void acceptTermsAndConditions() {
        try {
            // Find the "I agree to the terms & conditions" checkbox specifically
            List<Locator> boxes = page.locator("input[type='checkbox']").all();
            for (Locator cb : boxes) {
                try {
                    String parentText = cb.evaluate(
                            "el => el.closest('label,div,p')?.innerText || ''").toString();
                    if (parentText.toLowerCase().contains("terms")) {
                        if (!cb.isChecked()) cb.click();
                        System.out.println("✔ Terms checkbox checked");
                        return;
                    }
                } catch (Exception ignored) {}
            }
            // Fallback: last unchecked checkbox is typically the terms one
            for (int i = boxes.size() - 1; i >= 0; i--) {
                if (!boxes.get(i).isChecked()) {
                    boxes.get(i).click();
                    System.out.println("✔ Terms checked via fallback");
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ Terms checkbox: " + e.getMessage());
        }
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    public void clickCompletePurchase() {
        waitForVisible(BTN_BUY);
        click(BTN_BUY);
    }

    // ── Validation Checks ─────────────────────────────────────────────────────

    public boolean hasValidationErrors() {
        try {
            // Check for HTML5 native validation (input:invalid)
            int invalidCount = page.locator("input:invalid, select:invalid").count();
            if (invalidCount > 0) return true;
            // Check for custom error message elements
            int errorMsgCount = page.locator(VALIDATION_ERRORS).count();
            return errorMsgCount > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasPaymentError() {
        try {
            return page.locator(PAYMENT_ERRORS).count() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private void safeFill(String selector, String value) {
        try {
            page.locator(selector).first().fill(value);
        } catch (Exception e) {
            System.out.println("⚠ safeFill [" + selector + "]: " + e.getMessage());
        }
    }

    private void safeSelectLabel(String selector, String label) {
        try {
            page.locator(selector).first()
                    .selectOption(new SelectOption().setLabel(label));
        } catch (Exception e) {
            try {
                page.locator(selector).first().selectOption(label);
            } catch (Exception ex) {
                System.out.println("⚠ safeSelect [" + selector + "] = " + label + ": " + ex.getMessage());
            }
        }
    }
}
