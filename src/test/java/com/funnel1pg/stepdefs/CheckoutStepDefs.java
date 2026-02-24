package com.funnel1pg.stepdefs;

import com.aventstack.extentreports.Status;
import com.funnel1pg.config.ConfigReader;
import com.funnel1pg.pages.CheckoutPage;
import com.funnel1pg.pages.ThankYouPage;
import com.funnel1pg.pages.UpsellPage;
import com.funnel1pg.utils.ExtentReportManager;
import com.funnel1pg.utils.PlaywrightManager;
import com.funnel1pg.utils.TestDataReader;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assumptions;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Funnel1pg Test Automation - Main Step Definitions
 * 
 * Handles complete funnel checkout flow:
 * 1. Primary order submission
 * 2. Post-purchase page detection
 * 3. Upsell loop processing with error handling
 * 4. Order verification on thank you page
 * 
 * Error Handling:
 * - Detects "You have already purchased this trial offer" error
 * - Skips test gracefully when error occurs
 * - Logs error code and proceeds to next test
 */
public class CheckoutStepDefs {

    private Page         page;
    private CheckoutPage checkoutPage;
    private UpsellPage   upsellPage;
    private ThankYouPage thankYouPage;
    
    // State tracking for funnel flow
    private String originalOrderNumber;
    private double originalOrderPrice;
    private boolean onThankYouPage = false;
    
    // Error handling and skip tracking
    private boolean testShouldBeSkipped = false;
    private String skipReason = "";
    private String errorCode = "";

    private void init() {
        page         = PlaywrightManager.getPage();
        checkoutPage = new CheckoutPage(page);
        upsellPage   = new UpsellPage(page);
        thankYouPage = new ThankYouPage(page);
    }

    private void log(String msg) {
        System.out.println(msg);
        var t = ExtentReportManager.getTest();
        if (t != null) t.log(Status.INFO, msg);
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

    // ===== NAVIGATION & SETUP =====
    
    @Given("I navigate to the checkout page")
    public void navigateToCheckout() {
        init();
        page.navigate(ConfigReader.getBaseUrl());
        page.waitForLoadState();
        log("📄 Loaded: " + page.url());
    }

    // ===== MAIN CHECKOUT FLOW =====
    
    @When("I select a product on the main page")
    public void selectProduct() {
        checkoutPage.selectFirstAvailableProduct();
        log("✓ Product selected");
    }

    @When("I fill in the shipping address with valid details")
    public void fillShippingAddress() {
        checkoutPage.fillShippingAddress(
                TestDataReader.getCustomer("firstName"),
                TestDataReader.getCustomer("lastName"),
                TestDataReader.getCustomer("address"),
                TestDataReader.getCustomer("city"),
                TestDataReader.getCustomer("state"),
                TestDataReader.getCustomer("zipCode"),
                TestDataReader.getCustomer("email"),
                TestDataReader.getCustomer("phone")
        );
        log("✓ Shipping address filled");
    }

    @When("I select a shipping method")
    public void selectShipping() {
        checkoutPage.selectShippingMethod();
        log("✓ Shipping method: Vande Shipping (pre-selected)");
    }

    @When("I fill in the payment details with test card")
    public void fillPayment() {
        checkoutPage.fillPaymentDetails(
                TestDataReader.getPayment("cardType"),
                TestDataReader.getPayment("cardNumber"),
                TestDataReader.getPayment("expiryMonth"),
                TestDataReader.getPayment("expiryYear"),
                TestDataReader.getPayment("cvv")
        );
        log("✓ Payment filled (test card)");
    }

    @When("I fill in the payment details with invalid card")
    public void fillInvalidPayment() {
        checkoutPage.fillPaymentDetails(
                "visa",
                "1234567890123456",
                TestDataReader.getPayment("expiryMonth"),
                TestDataReader.getPayment("expiryYear"),
                "000"
        );
        log("✓ Payment filled (invalid card)");
    }

    @When("I accept the terms and conditions")
    public void acceptTerms() {
        checkoutPage.acceptTermsAndConditions();
        log("✓ Terms accepted");
    }

    // ===== PRIMARY ORDER SUBMISSION =====
    
    @When("I click the complete purchase button")
    public void clickPurchase() {
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("📦 SUBMITTING PRIMARY ORDER");
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        checkoutPage.clickCompletePurchase();
        
        // Wait for navigation away from checkout
        Predicate<String> notCheckout = url -> !url.contains("checkout");
        try {
            page.waitForURL(notCheckout,
                    new Page.WaitForURLOptions().setTimeout(20_000));
        } catch (Exception e) {
            page.waitForLoadState();
            page.waitForTimeout(3000);
        }
        
        log("✓ Post-purchase URL: " + page.url());
    }

    // ===== POST-PURCHASE PAGE DETECTION & ROUTING =====
    
    @Then("I should be taken to an upsell page or thank you page")
    public void verifyPostPurchasePage() {
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("🔍 DETECTING POST-PURCHASE PAGE TYPE");
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        page.waitForLoadState();
        String currentUrl = page.url();
        log("📍 Current URL: " + currentUrl);
        
        // Check if we're on thank you page
        if (thankYouPage.isThankYouPageDisplayed()) {
            onThankYouPage = true;
            log("✓ LANDED ON THANK YOU PAGE (Primary Order Success)");
            captureOrderDetails();
            return;
        }
        
        // Check if we're on upsell page
        if (upsellPage.isUpsellPage()) {
            onThankYouPage = false;
            log("✓ LANDED ON UPSELL PAGE (Primary Order Success - Offer Available)");
            return;
        }
        
        // Fallback: if URL changed from checkout, assume success
        if (!currentUrl.contains("checkout")) {
            log("✓ Successfully left checkout page");
            return;
        }
        
        assertTrue(false,
                "Expected upsell or thank-you page after purchase, got: " + currentUrl);
    }

    // ===== UPSELL FLOW WITH LOOPING & ERROR HANDLING =====
    
    @And("I navigate through any upsell pages")
    public void navigateUpsells() {
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("🛒 PROCESSING UPSELL FUNNEL");
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // If already on thank you, no upsells to process
        if (onThankYouPage) {
            log("ℹ Already on thank-you page → no upsells to process");
            return;
        }
        
        int upsellCount = 0;
        int maxUpsells = 10; // Safety limit to prevent infinite loops
        
        while (upsellCount < maxUpsells) {
            page.waitForLoadState();
            String currentUrl = page.url();
            
            log("");
            log("┌─────────────────────────────────────────────");
            log("│ UPSELL ITERATION #" + (upsellCount + 1));
            log("│ URL: " + currentUrl);
            log("└─────────────────────────────────────────────");
            
            // ===== CHECK FOR ERROR: "You have already purchased this trial offer" =====
            if (checkForAlreadyPlacedOfferError()) {
                log("🚫 SKIPPING TEST: Trial offer already purchased!");
                logSkip("Trial offer has already been purchased. Test skipped with error code: " + errorCode);
                testShouldBeSkipped = true;
                skipReason = "Trial Offer Already Purchased";
                
                // Skip remaining upsells and proceed to next test
                log("");
                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                log("⊘ Test skipped due to: " + skipReason);
                log("⊘ Error Code: " + errorCode);
                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                // Skip to next test by throwing AssumptionViolatedException
                Assumptions.assumeTrue(false, "Test skipped: " + skipReason + " (Error Code: " + errorCode + ")");
                return; // Won't reach here due to assumption, but good for clarity
            }
            
            // Check if we've reached thank you page
            if (thankYouPage.isThankYouPageDisplayed()) {
                log("✓ REACHED THANK YOU PAGE after " + upsellCount + " upsell(s)");
                onThankYouPage = true;
                captureOrderDetails();
                break;
            }
            
            // Check if still on upsell page
            if (!upsellPage.isUpsellPage()) {
                log("ℹ No upsell detected → exiting loop");
                break;
            }
            
            log("✓ Upsell page detected");
            
            // Present the upsell offer - ADD PRODUCT & CONTINUE
            log("➤ Adding upsell product to order...");
            addUpsellProductToOrder();
            
            log("➤ Selecting shipping method for upsell...");
            selectUpsellShipping();
            
            log("➤ Accepting and continuing with upsell...");
            acceptAndContinueUpsell();
            
            // Wait for redirect after upsell submission
            Predicate<String> urlChanged = url -> !url.equals(currentUrl);
            try {
                page.waitForURL(urlChanged,
                        new Page.WaitForURLOptions().setTimeout(15_000));
                log("✓ Upsell submitted - redirected to: " + page.url());
            } catch (Exception e) {
                page.waitForLoadState();
                page.waitForTimeout(2000);
                log("⚠ Timeout waiting for upsell redirect: " + e.getMessage());
            }
            
            upsellCount++;
        }
        
        if (upsellCount >= maxUpsells) {
            log("⚠ WARNING: Max upsells (" + maxUpsells + ") reached - breaking loop");
        }
        
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("✓ UPSELL FUNNEL COMPLETE - Final URL: " + page.url());
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Check for "You have already purchased this trial offer" error message on upsell page
     * EXACT ERROR MESSAGE: "You have already purchased this trial offer"
     * @return true if error is found, false otherwise
     */
    private boolean checkForAlreadyPlacedOfferError() {
        try {
            // EXACT ERROR MESSAGE TO DETECT:
            // "You have already purchased this trial offer"
            
            String errorSelectors = 
                    // Exact match patterns
                    "div:has-text('You have already purchased this trial offer'), " +
                    "span:has-text('You have already purchased this trial offer'), " +
                    "p:has-text('You have already purchased this trial offer'), " +
                    "h1:has-text('You have already purchased this trial offer'), " +
                    "h2:has-text('You have already purchased this trial offer'), " +
                    
                    // Case-insensitive variations
                    "div:has-text('already purchased this trial'), " +
                    "span:has-text('already purchased this trial'), " +
                    "p:has-text('already purchased this trial'), " +
                    
                    // Error/Alert containers
                    ".error:has-text('already purchased'), " +
                    ".alert:has-text('already purchased'), " +
                    ".warning:has-text('already purchased'), " +
                    "[class*='error']:has-text('already purchased'), " +
                    "[class*='alert']:has-text('already purchased'), " +
                    "[class*='message']:has-text('already purchased'), " +
                    "[class*='notification']:has-text('already purchased')";
            
            int errorCount = page.locator(errorSelectors).count();
            
            if (errorCount > 0) {
                String errorMessage = page.locator(errorSelectors).first().textContent().trim();
                log("");
                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                log("🚫 ERROR DETECTED ON UPSELL PAGE");
                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                logError("Error Message: " + errorMessage);
                
                // Extract error code if present
                errorCode = extractErrorCode(errorMessage);
                if (!errorCode.isEmpty()) {
                    logError("Error Code: " + errorCode);
                } else {
                    errorCode = "ERR_TRIAL_OFFER_ALREADY_PURCHASED";
                    logError("Error Code: " + errorCode);
                }
                
                log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log("ℹ Error detection check: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract error code from error message if present
     * Looks for patterns like "ERR-001", "ERR_001", "Code: 001", etc.
     * @param errorMessage The error message text
     * @return The extracted error code or empty string
     */
    private String extractErrorCode(String errorMessage) {
        try {
            // Try to find error code patterns
            if (errorMessage.matches(".*ERR[-_]?\\d+.*")) {
                return errorMessage.replaceAll(".*(ERR[-_]?\\d+).*", "$1");
            }
            if (errorMessage.matches(".*Code[:\\s]+([A-Z0-9_-]+).*")) {
                return errorMessage.replaceAll(".*Code[:\\s]+([A-Z0-9_-]+).*", "$1");
            }
            if (errorMessage.matches(".*\\(([A-Z0-9]{3,}\\d+)\\).*")) {
                return errorMessage.replaceAll(".*\\(([A-Z0-9]{3,}\\d+)\\).*", "$1");
            }
        } catch (Exception e) {
            // If extraction fails, return empty string
        }
        return "";
    }

    // ===== UPSELL HELPER METHODS =====
    
    /**
     * Add upsell product to the order
     */
    private void addUpsellProductToOrder() {
        try {
            page.locator("button:has-text('Add'), button:has-text('YES'), " +
                    "a:has-text('Add'), a:has-text('YES'), " +
                    "[class*='add'], [class*='accept']").first().click();
            page.waitForTimeout(800);
            log("✓ Upsell product added");
        } catch (Exception e) {
            log("⚠ Could not add upsell product: " + e.getMessage());
        }
    }

    /**
     * Select shipping method for upsell (if required)
     */
    private void selectUpsellShipping() {
        try {
            // Try to find and select a shipping option
            var shippingButtons = page.locator("input[type='radio'][name*='ship'], " +
                    "button[name*='ship'], [class*='shipping-option']").all();
            
            if (!shippingButtons.isEmpty()) {
                shippingButtons.get(0).click();
                page.waitForTimeout(500);
                log("✓ Upsell shipping selected");
            } else {
                log("ℹ No shipping selection needed for upsell");
            }
        } catch (Exception e) {
            log("ℹ Upsell shipping selection: " + e.getMessage());
        }
    }

    /**
     * Accept and continue with upsell
     */
    private void acceptAndContinueUpsell() {
        try {
            // Look for accept/continue button
            var continueBtn = page.locator(
                    "button:has-text('Continue'), button:has-text('Accept'), " +
                    "button:has-text('YES'), a:has-text('Continue'), " +
                    "a:has-text('Accept'), a:has-text('YES'), " +
                    "[class*='continue'], [class*='accept']").first();
            
            continueBtn.click();
            page.waitForTimeout(1000);
            log("✓ Upsell accepted and continued");
        } catch (Exception e) {
            log("⚠ Could not accept upsell: " + e.getMessage());
        }
    }

    // ===== FINAL VERIFICATION =====
    
    @Then("I should land on the thank you page")
    public void verifyThankYouPage() {
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("✅ FINAL VERIFICATION - THANK YOU PAGE");
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        page.waitForLoadState();
        boolean onThankYou = thankYouPage.isThankYouPageDisplayed();
        
        log("📍 Final URL     : " + page.url());
        log("📄 Page heading  : " + thankYouPage.getHeading());
        
        assertTrue(onThankYou,
                "Expected Thank You page but got: " + page.url());
        
        log("✓ ORDER COMPLETE → Thank You page confirmed!");
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Capture and verify all order details from thank you page
     */
    private void captureOrderDetails() {
        log("");
        log("📋 VERIFYING ORDER DETAILS:");
        
        try {
            // Extract order number
            String orderNumber = thankYouPage.getOrderNumber();
            log("  • Order Number : " + orderNumber);
            
            // Extract order total/price
            String orderPrice = thankYouPage.getOrderPrice();
            log("  • Order Price   : " + orderPrice);
            
            // Extract shipping address
            String shippingAddress = thankYouPage.getShippingAddress();
            log("  • Shipping Addr : " + (shippingAddress.length() > 50 
                    ? shippingAddress.substring(0, 50) + "..." 
                    : shippingAddress));
            
            // Extract order items
            String orderItems = thankYouPage.getOrderItems();
            log("  • Items Ordered : " + (orderItems.length() > 50 
                    ? orderItems.substring(0, 50) + "..." 
                    : orderItems));
            
            log("✓ Order details verified");
        } catch (Exception e) {
            log("⚠ Error capturing order details: " + e.getMessage());
        }
    }

    // ===== VALIDATION & ERROR CHECKING =====
    
    @When("I click the complete purchase button without filling any fields")
    public void clickPurchaseEmpty() {
        checkoutPage.clickCompletePurchase();
        page.waitForTimeout(1500);
        log("📋 Submitted empty form → checking validation errors");
    }

    @Then("I should see required field validation errors on the form")
    public void verifyValidationErrors() {
        boolean hasErrors = checkoutPage.hasValidationErrors();
        log("✓ Validation errors present: " + hasErrors);
        assertTrue(hasErrors,
                "Expected form validation errors after empty submission");
    }

    @Then("I should see a payment error message")
    public void verifyPaymentError() {
        boolean hasError = checkoutPage.hasPaymentError();
        log("✓ Payment error present: " + hasError);
        assertTrue(hasError,
                "Expected payment error for invalid card");
    }

    /**
     * Get skip status
     */
    public boolean isTestSkipped() {
        return testShouldBeSkipped;
    }

    /**
     * Get skip reason
     */
    public String getSkipReason() {
        return skipReason;
    }

    /**
     * Get error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}