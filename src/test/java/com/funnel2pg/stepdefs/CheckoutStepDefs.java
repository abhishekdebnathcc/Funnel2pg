package com.funnel2pg.stepdefs;

import com.aventstack.extentreports.Status;
import com.funnel2pg.config.ConfigReader;
import com.funnel2pg.pages.CheckoutPage;
import com.funnel2pg.pages.LandingPage;
import com.funnel2pg.pages.ThankYouPage;
import com.funnel2pg.pages.UpsellPage;
import com.funnel2pg.utils.DataRandomizer;
import com.funnel2pg.utils.ExtentReportManager;
import com.funnel2pg.utils.PlaywrightManager;
import com.funnel2pg.utils.TestDataReader;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assumptions;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CheckoutStepDefs – step definitions for the 2-page funnel flow.
 *
 * FLOW:
 *   Page 1 (/landing)   → Prospect form (shipping address)
 *   Page 2 (/checkout)  → Payment form
 *   Upsells             → accept & continue loop
 *   Thank You           → order confirmation
 *
 * PAGE DETECTION uses <meta name="page-type"> injected by the CMS:
 *   "landing"   → LandingPage
 *   "checkout"  → CheckoutPage
 *   "upsell"    → UpsellPage
 *   "thank-you" → ThankYouPage
 */
public class CheckoutStepDefs {

    private Page         page;
    private LandingPage  landingPage;
    private CheckoutPage checkoutPage;
    private UpsellPage   upsellPage;

    // Tracks all products ordered with their type label (Main, Cross-sell, Upsell)
    // Each entry: [label, productName]  e.g. ["Main Product", "Wrist Watch $1.75"]
    private final java.util.List<String[]> orderedItems = new java.util.ArrayList<>();
    private ThankYouPage thankYouPage;

    private boolean onThankYouPage      = false;
    private boolean testShouldBeSkipped = false;
    private String  skipReason          = "";
    private String  errorCode           = "";

    private static final int MAX_UPSELLS = 10;

    // ── Init ──────────────────────────────────────────────────────────────────

    private void init() {
        page         = PlaywrightManager.getPage();
        landingPage  = new LandingPage(page);
        checkoutPage = new CheckoutPage(page);
        upsellPage   = new UpsellPage(page);
        thankYouPage = new ThankYouPage(page);
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    private void log(String msg) {
        System.out.println(msg);
        var t = ExtentReportManager.getTest();
        if (t != null) t.log(Status.INFO, msg);
    }

    private void logWarn(String msg) {
        System.out.println("⚠ " + msg);
        var t = ExtentReportManager.getTest();
        if (t != null) t.log(Status.WARNING, msg);
    }

    private void logError(String msg) {
        System.out.println("❌ " + msg);
        var t = ExtentReportManager.getTest();
        if (t != null) t.log(Status.WARNING, msg);
    }

    private void logSkip(String msg) {
        System.out.println("⊘ SKIPPED: " + msg);
        var t = ExtentReportManager.getTest();
        if (t != null) t.log(Status.SKIP, "SKIPPED: " + msg);
    }

    private void divider(String label) {
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log(label);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getMetaPageType() {
        try {
            String val = page.getAttribute("meta[name='page-type']", "content");
            return val != null ? val.trim().toLowerCase() : "";
        } catch (Exception e) {
            return "";
        }
    }

    // =========================================================================
    // GIVEN – Navigation
    // =========================================================================

    @Given("I navigate to the landing page")
    public void navigateToLanding() {
        init();
        page.navigate(ConfigReader.getLandingUrl());
        page.waitForLoadState();
        log("📄 Loaded landing page: " + page.url());
    }

    // =========================================================================
    // WHEN – Landing Page (Step 1)
    // =========================================================================

    @When("I fill in the prospect form with valid shipping details")
    public void fillProspectForm() {
        divider("📝 FILLING LANDING PAGE PROSPECT FORM");
        DataRandomizer.printIdentity();
        landingPage.fillProspectFormAndSubmit(
                DataRandomizer.getCustomerField("firstName"),
                DataRandomizer.getCustomerField("lastName"),
                DataRandomizer.getCustomerField("address"),
                DataRandomizer.getCustomerField("city"),
                DataRandomizer.getCustomerField("zipCode"),
                DataRandomizer.getCustomerField("phone"),
                DataRandomizer.getCustomerField("email")
        );
        log("✓ Prospect form filled");
    }

    @When("I click Rush My Order")
    public void clickRushMyOrder() {
        // Form is submitted inside fillProspectFormAndSubmit; wait for the server redirect
        divider("🚀 RUSH MY ORDER SUBMITTED – Waiting for server redirect");
        String landingUrl = page.url();
        try {
            // Wait until URL changes away from current page
            page.waitForURL(url -> !url.equals(landingUrl),
                    new Page.WaitForURLOptions().setTimeout(20_000));
        } catch (Exception e) {
            page.waitForLoadState();
            page.waitForTimeout(3_000);
        }
        captureAndLogPageError("After Landing Submit");
        log("✓ Post-submit URL: " + page.url());
    }

    @When("I click Rush My Order without filling any fields")
    public void clickRushMyOrderEmpty() {
        // Just click the submit button without filling form
        try {
            page.locator("button.button-submit").click();
        } catch (Exception e) {
            System.out.println("✗ button-submit click: " + e.getMessage());
        }
        page.waitForTimeout(1_500);
    }

    // =========================================================================
    // THEN – Redirect to Checkout
    // =========================================================================

    @Then("I should be redirected to the checkout page")
    public void verifyRedirectedToCheckout() {
        String url      = page.url();
        String pageType = getMetaPageType();
        log("📍 URL       : " + url);
        log("📌 page-type : " + pageType);
        assertTrue(
                "checkout".equals(pageType) || url.toLowerCase().contains("/checkout"),
                "Expected redirect to checkout after landing form submit, got: " + url
        );
        log("✓ On checkout page");
    }

    // =========================================================================
    // WHEN – Checkout Form (Step 2)
    // =========================================================================

    @When("I select a product on the checkout page")
    public void selectProduct() {
        checkoutPage.selectFirstAvailableProduct();
        log("✓ Product selected");
        // Record main product(s)
        for (String name : checkoutPage.getSelectedMainProductNames()) {
            orderedItems.add(new String[]{"Main Product", name});
        }
        // Click all cross-sell products then read which were selected
        checkoutPage.selectAllCrossSellProducts();
        java.util.List<String> crossNames = checkoutPage.getSelectedCrossSellNames();
        for (String csName : crossNames) {
            orderedItems.add(new String[]{"Cross-sell", csName});
        }
        if (!crossNames.isEmpty()) log("✓ Cross-sell products selected: " + crossNames.size());
    }

    @When("I select a shipping method")
    public void selectShipping() {
        checkoutPage.selectShippingMethod();
        log("✓ Shipping method set");
    }

    @When("I fill in the payment details with test card")
    public void fillPayment() {
        String method = ConfigReader.getPaymentMethod();
        log("💳 Payment method: " + method.toUpperCase());
        if ("cod".equalsIgnoreCase(method)) {
            checkoutPage.selectCashOnDelivery();
            log("✓ Cash on Delivery selected");
        } else {
            checkoutPage.selectAndFillCreditCard(
                    TestDataReader.getPayment("cardNumber"),
                    TestDataReader.getPayment("expiryMonth"),
                    TestDataReader.getPayment("expiryYear"),
                    TestDataReader.getPayment("cvv")
            );
            log("✓ Payment filled (Credit Card)");
        }
    }

    @When("I fill in the payment details with invalid card")
    public void fillInvalidPayment() {
        checkoutPage.selectAndFillCreditCard(
                "1234567890123456",
                TestDataReader.getPayment("expiryMonth"),
                TestDataReader.getPayment("expiryYear"),
                "000"
        );
        log("✓ Invalid payment filled");
    }

    @When("I accept the terms and conditions")
    public void acceptTerms() {
        checkoutPage.acceptTermsAndConditions();
        log("✓ Terms accepted");
    }

    @When("I click the complete purchase button")
    public void clickPurchase() {
        divider("📦 SUBMITTING ORDER");
        checkoutPage.clickCompletePurchase();

        Predicate<String> leftCheckout = url -> !url.toLowerCase().contains("/checkout");
        try {
            page.waitForURL(leftCheckout, new Page.WaitForURLOptions().setTimeout(20_000));
        } catch (Exception e) {
            page.waitForLoadState();
            page.waitForTimeout(3_000);
        }
        log("✓ Post-submit URL: " + page.url());
    }

    // =========================================================================
    // THEN – Post-Purchase Page Detection
    // =========================================================================

    @Then("I should be taken to an upsell page or thank you page")
    public void verifyPostPurchasePage() {
        divider("🔍 DETECTING POST-PURCHASE PAGE TYPE");
        page.waitForLoadState();

        String url      = page.url();
        String pageType = getMetaPageType();

        log("📍 URL      : " + url);
        log("📌 page-type: " + (pageType.isEmpty() ? "(meta not found)" : pageType));

        if ("thank-you".equals(pageType) || thankYouPage.isThankYouPageDisplayed()) {
            onThankYouPage = true;
            log("✓ Landed on THANK YOU PAGE");
            captureAndLogOrderDetails();
            return;
        }

        if ("upsell".equals(pageType) || upsellPage.isUpsellPage()) {
            onThankYouPage = false;
            log("✓ Landed on UPSELL PAGE");
            return;
        }

        if (!url.toLowerCase().contains("/checkout")) {
            logWarn("Page type undetermined but left checkout – continuing");
            return;
        }

        assertTrue(false, "Expected upsell or thank-you page after purchase, got: " + url);
    }

    // =========================================================================
    // AND – Upsell Loop
    // =========================================================================

    @And("I navigate through any upsell pages")
    public void navigateUpsells() {
        divider("🛒 PROCESSING UPSELL FUNNEL");

        if (onThankYouPage) {
            log("ℹ Already on Thank-You page – no upsells to process");
            return;
        }

        int upsellCount = 0;

        while (upsellCount < MAX_UPSELLS) {
            page.waitForLoadState();
            String currentUrl = page.url();
            String pageType   = getMetaPageType();

            log("");
            log("┌──────────────────────────────────────────────────");
            log("│ ITERATION #" + (upsellCount + 1) + "  |  page-type: " + pageType);
            log("│ URL: " + currentUrl);
            log("└──────────────────────────────────────────────────");

            if (detectAlreadyPurchasedError()) {
                handleAlreadyPurchasedError();
                return;
            }

            if ("thank-you".equals(pageType) || thankYouPage.isThankYouPageDisplayed()) {
                log("✓ THANK YOU PAGE reached after " + upsellCount + " upsell(s)");
                onThankYouPage = true;
                captureAndLogOrderDetails();
                break;
            }

            if (!"upsell".equals(pageType) && !upsellPage.isUpsellPage()) {
                logWarn("Page is neither upsell nor thank-you – exiting loop");
                break;
            }

            log("✓ On UPSELL PAGE – processing offer");
            log("  ➤ Step 1: Activate product (.sel-prod)");
            String upsellName = upsellPage.getUpsellProductName();
            upsellPage.addProductToUpsell();
            log("  ➤ Step 2: Select shipping (.shipping-option.shipping-method-div)");
            upsellPage.selectUpsellShipping();
            log("  ➤ Step 3: Submit (#upsell_form button.send-btn)");
            upsellPage.acceptAndContinue();
            orderedItems.add(new String[]{"Upsell", upsellName});

            // Upsell form submits via AJAX (place-upsell), then JS navigates.
            // Wait for network idle so the CRM response + redirect chain settles.
            try {
                page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                        new Page.WaitForLoadStateOptions().setTimeout(20_000));
            } catch (Exception ignored) {}

            String newUrl = page.url();
            if (!newUrl.equals(currentUrl)) {
                log("  ✓ Redirected to: " + newUrl);
            } else {
                // URL still same – this funnel uses in-place DOM swap (JS replaces
                // meta[page-type] without a navigation). Poll for up to 10s.
                String afterType = "upsell";
                long deadline = System.currentTimeMillis() + 10_000;
                while (System.currentTimeMillis() < deadline) {
                    page.waitForTimeout(500);
                    newUrl = page.url();
                    if (!newUrl.equals(currentUrl)) break;   // navigation happened
                    afterType = getMetaPageType();
                    if (!"upsell".equals(afterType)) break;  // DOM swapped in-place
                }

                if (!newUrl.equals(currentUrl)) {
                    log("  ✓ Redirected to: " + newUrl);
                } else if ("thank-you".equals(afterType) || thankYouPage.isThankYouPageDisplayed()) {
                    log("  ✓ Page transitioned to thank-you in-place");
                    onThankYouPage = true;
                    captureAndLogOrderDetails();
                    break;
                } else if (!"upsell".equals(afterType) && !afterType.isEmpty()) {
                    log("  ✓ Page type changed to: " + afterType);
                } else {
                    logWarn("URL and page-type unchanged after 10s – breaking loop");
                    break;
                }
            }

            upsellCount++;
        }

        if (upsellCount >= MAX_UPSELLS) {
            logWarn("MAX_UPSELLS (" + MAX_UPSELLS + ") reached – stopping loop");
        }

        divider("✓ UPSELL FUNNEL COMPLETE – Final URL: " + page.url());
    }

    // =========================================================================
    // THEN – Final Verification
    // =========================================================================

    @Then("I should land on the thank you page")
    public void verifyThankYouPage() {
        divider("✅ FINAL VERIFICATION – THANK YOU PAGE");
        page.waitForLoadState();

        String pageType = getMetaPageType();
        log("📍 Final URL    : " + page.url());
        log("📌 page-type    : " + pageType);
        log("📄 Page heading : " + thankYouPage.getHeading());

        boolean confirmed = "thank-you".equals(pageType) || thankYouPage.isThankYouPageDisplayed();
        assertTrue(confirmed, "Expected Thank-You page but got: " + page.url());

        if (!onThankYouPage) {
            captureAndLogOrderDetails();
        }

        log("✓ ORDER COMPLETE → Thank-You page confirmed!");
    }

    // =========================================================================
    // VALIDATION SCENARIOS
    // =========================================================================

    @Then("I should see required field validation errors on the landing form")
    public void verifyLandingValidationErrors() {
        boolean hasErrors = landingPage.hasValidationErrors();
        log("✓ Landing form validation errors: " + hasErrors);
        assertTrue(hasErrors, "Expected validation errors after empty landing form submission");
    }

    @Then("I should see a payment error message")
    public void verifyPaymentError() {
        boolean hasError = checkoutPage.hasPaymentError();
        log("✓ Payment error: " + hasError);
        assertTrue(hasError, "Expected payment error for invalid card");
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private void captureAndLogOrderDetails() {
        try {
            thankYouPage.waitForPageToPopulate();

            String orderId  = thankYouPage.getOrderNumber();
            String subtotal = thankYouPage.getSubtotal();
            String shipping = thankYouPage.getShippingTotal();
            String total    = thankYouPage.getOrderPrice();
            String name     = thankYouPage.getFullName();
            String email    = thankYouPage.getEmail();
            String phone    = thankYouPage.getPhone();
            String address  = thankYouPage.getStreetAddress();
            String city     = thankYouPage.getCity();
            String state    = thankYouPage.getState();
            String zip      = thankYouPage.getZip();
            String country  = thankYouPage.getCountry();
            java.util.List<String> items = thankYouPage.getOrderItemLines();

            System.out.println("");
            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│                   📋 ORDER DETAILS                  │");
            System.out.println("├─────────────────────────────────────────────────────┤");
            System.out.println("│  Order ID      : " + orderId);
            System.out.println("│  Name          : " + name);
            System.out.println("│  Email         : " + email);
            System.out.println("│  Phone         : " + phone);
            String addrLine = address;
            if (city != null && !city.equals("N/A") && !city.isEmpty()) addrLine += ", " + city;
            addrLine += ", " + state + " " + zip;
            System.out.println("│  Address       : " + addrLine);
            System.out.println("│  Country       : " + country);
            // Print items with labels from tracking list if available,
            // otherwise fall back to thank-you page DOM items
            if (!orderedItems.isEmpty()) {
                for (String[] entry : orderedItems) {
                    System.out.printf("│  %-14s: %s%n", entry[0], entry[1]);
                }
            } else if (items.isEmpty()) {
                System.out.println("│  Items         : " + thankYouPage.getOrderItems());
            } else {
                for (String item : items) System.out.println("│  Item          : " + item);
            }
            System.out.println("│  Subtotal      : " + subtotal);
            System.out.println("│  Shipping      : " + shipping);
            System.out.println("│  Total         : " + total);
            System.out.println("└─────────────────────────────────────────────────────┘");

            // Build item rows - use tracked labels if available, else fall back to DOM
            StringBuilder itemRows = new StringBuilder();
            if (!orderedItems.isEmpty()) {
                for (String[] entry : orderedItems) {
                    String itemLabel = entry[0];
                    String itemName  = entry[1];
                    String color = itemLabel.equals("Main Product") ? "#e8f5e9"
                                 : itemLabel.equals("Cross-sell")   ? "#fff8e1"
                                 :                                    "#f3e5f5"; // Upsell
                    itemRows.append("<tr style='background:").append(color).append(";'>")
                            .append("<td><em>").append(itemLabel).append("</em></td>")
                            .append("<td colspan='2'>").append(itemName).append("</td></tr>");
                }
            } else if (items.isEmpty()) {
                itemRows.append("<tr><td>Items</td><td colspan='2'>")
                        .append(thankYouPage.getOrderItems()).append("</td></tr>");
            } else {
                for (int i = 0; i < items.size(); i++) {
                    itemRows.append("<tr><td>").append(i == 0 ? "Items" : "")
                            .append("</td><td colspan='2'>").append(items.get(i)).append("</td></tr>");
                }
            }

            String html = "<div style='font-family:monospace;font-size:13px;'>"
                + "<table border='1' cellpadding='6' cellspacing='0' "
                + "style='border-collapse:collapse;width:100%;background:#f9f9ff;border:1px solid #c0c0e0;'>"
                + "<thead><tr style='background:#3f51b5;color:white;font-size:14px;'>"
                + "<th colspan='3'>📋 ORDER CONFIRMATION DETAILS</th></tr></thead>"
                + "<tbody>"
                + "<tr style='background:#e8f5e9;font-weight:bold;font-size:14px;'>"
                + "<td width='25%'>🔖 Order ID</td>"
                + "<td colspan='2' style='color:#1b5e20;font-size:15px;'>" + orderId + "</td></tr>"
                + "<tr style='background:#e3f2fd;'><td colspan='3'><strong>📦 Shipping Address</strong></td></tr>"
                + "<tr><td>Name</td><td colspan='2'>" + name + "</td></tr>"
                + "<tr><td>Email</td><td colspan='2'>" + email + "</td></tr>"
                + "<tr><td>Phone</td><td colspan='2'>" + phone + "</td></tr>"
                + "<tr><td>Address</td><td colspan='2'>" + address + "</td></tr>"
                + "<tr><td>City</td><td colspan='2'>" + city + "</td></tr>"
                + "<tr><td>State</td><td colspan='2'>" + state + "</td></tr>"
                + "<tr><td>Zip</td><td colspan='2'>" + zip + "</td></tr>"
                + "<tr><td>Country</td><td colspan='2'>" + country + "</td></tr>"
                + "<tr style='background:#fff3e0;'><td colspan='3'><strong>🛒 Items Ordered</strong></td></tr>"
                + itemRows
                + "<tr style='background:#fce4ec;'><td colspan='3'><strong>💰 Order Totals</strong></td></tr>"
                + "<tr><td>Subtotal</td><td colspan='2'>" + subtotal + "</td></tr>"
                + "<tr><td>Shipping</td><td colspan='2'>" + shipping + "</td></tr>"
                + "<tr style='background:#e8f5e9;font-weight:bold;font-size:14px;'>"
                + "<td>Total</td><td colspan='2' style='color:#1b5e20;'>" + total + "</td></tr>"
                + "</tbody></table></div>";

            var extentTest = ExtentReportManager.getTest();
            if (extentTest != null) {
                extentTest.log(Status.INFO, "<b>✅ ORDER CONFIRMED</b>");
                extentTest.info(com.aventstack.extentreports.markuputils.MarkupHelper
                        .createLabel("Order ID: " + orderId,
                                com.aventstack.extentreports.markuputils.ExtentColor.GREEN));
                extentTest.log(Status.INFO, html);
                try {
                    byte[] png = page.screenshot(
                            new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true));
                    String safeName = "thankyou_" + System.currentTimeMillis();
                    String screenshotPath = ConfigReader.getReportsDir()
                            + "/screenshots/" + safeName + ".png";
                    java.nio.file.Files.write(java.nio.file.Paths.get(screenshotPath), png);
                    extentTest.addScreenCaptureFromPath("screenshots/" + safeName + ".png",
                            "📸 Thank-You Page – Order Confirmation");
                } catch (Exception se) {
                    System.out.println("⚠ Could not capture thank-you screenshot: " + se.getMessage());
                }
            }

        } catch (Exception e) {
            logWarn("Could not fully capture order details: " + e.getMessage());
        }
    }

    // =========================================================================
    // ERROR DETECTION & REPORTING
    // =========================================================================

    /**
     * Scans the current page for any visible error messages using broad selectors.
     * Captures the text, extracts any error code, takes a screenshot, and logs
     * a rich formatted panel to the Extent report.
     *
     * @param context  human-readable label indicating where in the flow this was called
     * @return true if at least one error was found and captured
     */
    private boolean captureAndLogPageError(String context) {
        try {
            // Broad selectors covering inline errors, modal errors, toast errors, etc.
            String[] errorSelectors = {
                "#formError",
                "#inline-error",
                ".error-message",
                ".alert-danger",
                "[class*='error-msg']",
                "[class*='error-text']",
                "[class*='form-error']",
                "[id*='error']",
                "[class*='alert'][class*='error']",
                ".invalid-feedback",
                "[class*='decline']",
                "[class*='card-error']",
                "[class*='payment-error']",
                "div[style*='color:red']",
                "div[style*='color: red']",
                "span[style*='color:red']",
                "span[style*='color: red']"
            };

            java.util.List<String> found = new java.util.ArrayList<>();
            for (String sel : errorSelectors) {
                try {
                    var locator = page.locator(sel);
                    int count = locator.count();
                    for (int i = 0; i < count; i++) {
                        try {
                            var el = locator.nth(i);
                            if (el.isVisible()) {
                                String text = el.textContent().trim().replaceAll("\\s+", " ");
                                if (!text.isEmpty() && !found.contains(text)) {
                                    found.add(text);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }

            // Also check body text for known error phrases
            if (found.isEmpty()) {
                try {
                    String bodyText = page.locator("body").textContent().toLowerCase();
                    String[] knownPhrases = {
                        "already purchased this trial",
                        "transaction was declined",
                        "card was declined",
                        "invalid card",
                        "payment failed",
                        "something went wrong",
                        "error processing"
                    };
                    for (String phrase : knownPhrases) {
                        if (bodyText.contains(phrase)) {
                            found.add("(Detected in page body) " + phrase);
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (found.isEmpty()) return false;

            // Extract error code from first message
            String primaryMsg = found.get(0);
            errorCode = extractErrorCode(primaryMsg);
            if (errorCode.isEmpty() && primaryMsg.toLowerCase().contains("already purchased")) {
                errorCode = "ERR_TRIAL_OFFER_ALREADY_PURCHASED";
            }

            // Console output
            System.out.println("");
            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│                  ⚠ PAGE ERROR DETECTED              │");
            System.out.println("│  Context : " + context);
            System.out.println("│  URL     : " + page.url());
            for (String msg : found) {
                System.out.println("│  Error   : " + msg);
            }
            if (!errorCode.isEmpty()) {
                System.out.println("│  Code    : " + errorCode);
            }
            System.out.println("└─────────────────────────────────────────────────────┘");

            // Extent report – rich HTML panel
            var extentTest = ExtentReportManager.getTest();
            if (extentTest != null) {
                StringBuilder rows = new StringBuilder();
                for (String msg : found) {
                    rows.append("<tr><td style='padding:6px 10px;'>").append(msg).append("</td></tr>");
                }
                String errorPanel =
                    "<div style='font-family:monospace;margin:8px 0;'>" +
                    "<table style='width:100%;border-collapse:collapse;border:2px solid #c62828;border-radius:4px;overflow:hidden;'>" +
                    "<thead><tr style='background:#c62828;color:white;font-weight:bold;'>" +
                    "<th style='padding:8px 12px;text-align:left;'>&#x26A0; PAGE ERROR &nbsp;|&nbsp; Context: " + context + "</th></tr></thead>" +
                    "<tbody style='background:#fff8f8;'>" +
                    "<tr style='background:#ffebee;'><td style='padding:6px 10px;font-size:11px;color:#666;'>URL: " + page.url() + "</td></tr>" +
                    rows +
                    (errorCode.isEmpty() ? "" :
                        "<tr style='background:#fce4ec;font-weight:bold;'>" +
                        "<td style='padding:6px 10px;'>Error Code: " + errorCode + "</td></tr>") +
                    "</tbody></table></div>";

                extentTest.log(com.aventstack.extentreports.Status.WARNING, errorPanel);

                // Screenshot of the error state
                try {
                    byte[] png = page.screenshot(
                            new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(false));
                    String safeName = "error_" + context.replaceAll("[^a-zA-Z0-9]", "_")
                            + "_" + System.currentTimeMillis();
                    String screenshotPath = ConfigReader.getReportsDir()
                            + "/screenshots/" + safeName + ".png";
                    java.nio.file.Files.write(java.nio.file.Paths.get(screenshotPath), png);
                    extentTest.addScreenCaptureFromPath(
                            "screenshots/" + safeName + ".png",
                            "Error Screenshot – " + context);
                } catch (Exception se) {
                    System.out.println("⚠ Could not capture error screenshot: " + se.getMessage());
                }
            }

            return true;

        } catch (Exception e) {
            System.out.println("⚠ captureAndLogPageError failed: " + e.getMessage());
            return false;
        }
    }

    private boolean detectAlreadyPurchasedError() {
        try {
            String body = page.locator("body").textContent().toLowerCase();
            if (body.contains("already purchased this trial") || body.contains("already purchased")) {
                errorCode = "ERR_TRIAL_OFFER_ALREADY_PURCHASED";
                captureAndLogPageError("Already Purchased Check");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleAlreadyPurchasedError() {
        testShouldBeSkipped = true;
        skipReason = "Trial Offer Already Purchased";
        divider("⊘ SKIPPING TEST");
        logSkip("Reason    : " + skipReason);
        logSkip("Error Code: " + errorCode);
        Assumptions.assumeTrue(false,
                "Test skipped: " + skipReason + " (Error Code: " + errorCode + ")");
    }

    private String extractErrorCode(String msg) {
        if (msg == null || msg.isEmpty()) return "";
        try {
            // Match ERR-1234, ERR_1234, ERROR-123 patterns
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(ERR(?:OR)?[-_]?\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(msg);
            if (m.find()) return m.group(1).toUpperCase();
        } catch (Exception ignored) {}
        return "";
    }

    public boolean isTestSkipped() { return testShouldBeSkipped; }
    public String  getSkipReason() { return skipReason; }
    public String  getErrorCode()  { return errorCode; }
}
