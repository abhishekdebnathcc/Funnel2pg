package com.funnel2pg.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

/**
 * LandingPage – handles Step 1 of the 2pgCCF25Feb funnel.
 *
 * This is the prospect/shipping form at /landing.
 * The user fills in their address and clicks "Rush My Order".
 * On success, the CRM saves the prospect and redirects to /checkout.
 *
 * FORM SELECTORS (from DOM inspection of /2pgCCF25Feb/landing):
 *   form action="save-prospect"  name="prospect_form1"
 *   #shipFirstName   → firstName
 *   #shipLastName    → lastName
 *   #shipAddress1    → shippingAddress1
 *   #shippingCountry → shippingCountry (select)
 *   #shippingState   → shippingState   (select)
 *   #shippingCity    → shippingCity
 *   #shipPostalCode  → shippingZip
 *   #phoneNumber     → phone
 *   #emailAddress    → email
 *   button.button-submit → "Rush My Order"
 */
public class LandingPage extends BasePage {

    // ── Form field selectors ──────────────────────────────────────────────────
    private static final String INPUT_FIRST_NAME = "#shipFirstName";
    private static final String INPUT_LAST_NAME  = "#shipLastName";
    private static final String INPUT_ADDRESS    = "#shipAddress1";
    private static final String SELECT_COUNTRY  = "#shippingCountry";
    private static final String SELECT_STATE     = "#shippingState";
    private static final String INPUT_CITY       = "#shippingCity";
    private static final String INPUT_ZIP        = "#shipPostalCode";
    private static final String INPUT_PHONE      = "#phoneNumber";
    private static final String INPUT_EMAIL      = "#emailAddress";
    private static final String BTN_SUBMIT       = "button.button-submit";

    public LandingPage(Page page) { super(page); }

    /**
     * Fills all fields on the landing prospect form and submits it.
     * State is selected by value (e.g. "CA") since the select is populated dynamically.
     */
    public void fillProspectFormAndSubmit(String firstName, String lastName, String address,
                                          String city, String state, String zip,
                                          String phone, String email) {
        safeFill(INPUT_FIRST_NAME, firstName);
        safeFill(INPUT_LAST_NAME,  lastName);
        safeFill(INPUT_ADDRESS,    address);

        // Country defaults to US – wait for state dropdown to populate then select
        page.waitForTimeout(800);
        safeSelectByValue(SELECT_STATE, state);

        safeFill(INPUT_CITY,  city);
        safeFill(INPUT_ZIP,   zip);
        safeFill(INPUT_PHONE, phone);
        safeFill(INPUT_EMAIL, email);

        System.out.println("✓ Landing page prospect form filled");

        // Submit the form
        click(BTN_SUBMIT);
        System.out.println("✓ Rush My Order clicked");
    }

    /** Checks whether we're on the landing page (meta page-type=landing). */
    public boolean isLandingPage() {
        try {
            String val = page.getAttribute("meta[name='page-type']", "content");
            return "landing".equalsIgnoreCase(val != null ? val.trim() : "");
        } catch (Exception e) {
            return page.url().toLowerCase().contains("/landing");
        }
    }

    /** Returns true if the form submit button is visible and ready. */
    public boolean isFormReady() {
        return isVisible(BTN_SUBMIT);
    }

    /** Validation errors – inline error messages after failed submit. */
    public boolean hasValidationErrors() {
        try {
            return page.locator("input:invalid, select:invalid, .error, [class*='error-msg']").count() > 0;
        } catch (Exception e) { return false; }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void safeFill(String selector, String value) {
        try {
            page.locator(selector).fill(value);
        } catch (Exception e) {
            System.out.println("✗ safeFill [" + selector + "]: " + e.getMessage());
        }
    }

    private void safeSelectByValue(String selector, String value) {
        try {
            page.locator(selector).selectOption(new SelectOption().setValue(value));
        } catch (Exception e) {
            try {
                page.locator(selector).selectOption(value);
            } catch (Exception ex) {
                System.out.println("✗ safeSelect [" + selector + "]='" + value + "': " + ex.getMessage());
            }
        }
    }
}
