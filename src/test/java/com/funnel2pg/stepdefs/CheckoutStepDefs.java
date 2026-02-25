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
        landingPage.fillProspectFormAndSubmit(
                DataRandomizer.getCustomerField("firstName"),
                DataRandomizer.getCustomerField("lastName"),
                DataRandomizer.getCustomerField("address"),
                DataRandomizer.getCustomerField("city"),
                DataRandomizer.getCustomerField("state"),
                DataRandomizer.getCustomerField("zipCode"),
                DataRandomizer.getCustomerField("phone"),
                DataRandomizer.getCustomerField("email")
        );
        log("✓ Prospect form filled");
    }

    @When("I click Rush My Order")
    public void clickRushMyOrder() {
        // Form is submitted in fillProspectFormAndSubmit; wait for redirect to checkout
        divider("🚀 RUSH MY ORDER SUBMITTED – Waiting for /checkout redirect");
        Predicate<String> leftLanding = url -> !url.toLowerCase().contains("/landing");
        try {
            page.waitForURL(leftLanding, new Page.WaitForURLOptions().setTimeout(20_000));
        } catch (Exception e) {
            page.waitForLoadState();
            page.waitForTimeout(3_000);
        }
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
                "Expected redirect to /checkout after landing form submit, got: " + url
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
            log("  ➤ Step 1: Add product (a.btn-upsell)");
            upsellPage.addProductToUpsell();
            log("  ➤ Step 2: Select shipping (div.shipping-option.shipping-method-div)");
            upsellPage.selectUpsellShipping();
            log("  ➤ Step 3: Accept & Continue (button.submit-upsell-btn)");
            upsellPage.acceptAndContinue();

            Predicate<String> urlChanged = url -> !url.equals(currentUrl);
            try {
                page.waitForURL(urlChanged, new Page.WaitForURLOptions().setTimeout(15_000));
                log("  ✓ Redirected to: " + page.url());
            } catch (Exception e) {
                page.waitForLoadState();
                page.waitForTimeout(2_000);
                if (page.url().equals(currentUrl)) {
                    logWarn("URL unchanged after upsell submit – breaking loop");
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
            java.util.List<String> items = thankYouPage.getOrderItemLines();

            System.out.println("");
            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│                   📋 ORDER DETAILS                  │");
            System.out.println("├─────────────────────────────────────────────────────┤");
            System.out.println("│  Order ID      : " + orderId);
            System.out.println("│  Name          : " + name);
            System.out.println("│  Email         : " + email);
            System.out.println("│  Phone         : " + phone);
            System.out.println("│  Address       : " + address + ", " + city + ", " + state + " " + zip);
            if (items.isEmpty()) {
                System.out.println("│  Items         : " + thankYouPage.getOrderItems());
            } else {
                for (String item : items) System.out.println("│  Item          : " + item);
            }
            System.out.println("│  Subtotal      : " + subtotal);
            System.out.println("│  Shipping      : " + shipping);
            System.out.println("│  Total         : " + total);
            System.out.println("└─────────────────────────────────────────────────────┘");

            StringBuilder itemRows = new StringBuilder();
            if (items.isEmpty()) {
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

    private boolean detectAlreadyPurchasedError() {
        try {
            String selectors =
                    "div:has-text('already purchased this trial'), " +
                    "span:has-text('already purchased this trial'), " +
                    "p:has-text('already purchased this trial'), " +
                    ".error:has-text('already purchased'), " +
                    "#inline-error:has-text('already purchased'), " +
                    "[class*='error']:has-text('already purchased')";

            if (page.locator(selectors).count() > 0) {
                String text = page.locator(selectors).first().textContent().trim();
                logError("Already-purchased error: " + text);
                errorCode = extractErrorCode(text);
                if (errorCode.isEmpty()) errorCode = "ERR_TRIAL_OFFER_ALREADY_PURCHASED";
                return true;
            }

            String body = page.locator("body").textContent().toLowerCase();
            if (body.contains("already purchased this trial")) {
                logError("Already-purchased error detected via body text");
                errorCode = "ERR_TRIAL_OFFER_ALREADY_PURCHASED";
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
        try {
            if (msg.matches(".*ERR[-_]?\\d+.*"))
                return msg.replaceAll(".*(ERR[-_]?\\d+).*", "$1");
        } catch (Exception ignored) { }
        return "";
    }

    public boolean isTestSkipped() { return testShouldBeSkipped; }
    public String  getSkipReason() { return skipReason; }
    public String  getErrorCode()  { return errorCode; }
}
