# Funnel1pg Test Automation - Complete Business Logic Implementation

## Overview

This project implements comprehensive test automation for a multi-page funnel checkout flow with upsell pages, using Playwright and Cucumber BDD framework.

---

## 📋 Business Logic Implemented

### 1. **Primary Order Submission**
- Customer selects a product
- Fills checkout form with shipping, payment, and billing information
- Accepts terms & conditions
- Clicks "Complete Purchase"
- **System Action**: Submits the primary order

### 2. **Post-Purchase Redirect Detection** ✨
After successful primary order submission, the system redirects to ONE of two pages:

#### Option A: Direct to Thank You Page
- **Page Type**: Order confirmation/success page
- **Action**: Verify all order details (order #, pricing, address, items)
- **Next Step**: Complete (no more upsells)

#### Option B: Upsell Offer Page
- **Page Type**: One-time offer (OTO) page
- **Action**: Enter upsell processing loop
- **Next Step**: Process upsell flow

### 3. **Upsell Processing Loop** 🔄
For each upsell page encountered:

#### Step 1: Product Selection
- Detect upsell page content
- Select/add the upsell product to the order

#### Step 2: Shipping Selection (if required)
- Choose available shipping method
- Confirm selection

#### Step 3: Accept & Continue
- Click accept/continue button to submit upsell
- **Result**: Redirect to next page

### 4. **Loop Continuation**
After each upsell submission, the system again redirects to:
- **If Thank You Page**: Break loop and complete flow
- **If Another Upsell Page**: Repeat the upsell loop
- **Max Iterations**: 10 (safety limit to prevent infinite loops)

### 5. **Final Verification**
On reaching the Thank You page:
- ✓ Verify page is indeed Thank You/Confirmation
- ✓ Extract and verify order number
- ✓ Extract and verify order total price
- ✓ Extract and verify shipping address
- ✓ Extract and verify order items
- ✓ Log all details for test report

---

## 🏗️ Project Structure

```
src/test/
├── java/com/funnel1pg/
│   ├── pages/
│   │   ├── BasePage.java              # Base page object with common methods
│   │   ├── CheckoutPage.java          # Primary checkout form interactions
│   │   ├── UpsellPage.java            # Upsell page interactions (NEW!)
│   │   └── ThankYouPage.java          # Order confirmation (ENHANCED!)
│   ├── stepdefs/
│   │   └── CheckoutStepDefs.java      # Cucumber step implementations (NEW!)
│   ├── utils/
│   │   ├── PlaywrightManager.java     # Browser lifecycle management
│   │   ├── TestDataReader.java        # Test data loading
│   │   ├── ExtentReportManager.java   # Report generation
│   │   └── WaitUtils.java             # Explicit waits
│   ├── config/
│   │   └── ConfigReader.java          # Configuration management
│   ├── hooks/
│   │   └── Hooks.java                 # Cucumber hooks
│   └── runners/
│       ├── SmokeTestRunner.java       # Smoke test execution
│       ├── RegressionTestRunner.java  # Full test suite
│       └── ValidationTestRunner.java  # Validation tests
└── resources/
    ├── features/
    │   └── checkout_flow.feature      # BDD scenarios (UPDATED!)
    └── testdata/
        └── checkout_data.json         # Test data file
```

---

## 🔑 Key Components

### CheckoutStepDefs.java (ENHANCED)
The main step definitions file orchestrating the entire funnel flow:

**Primary Order Steps:**
- `@When("I select a product on the main page")`
- `@When("I fill in the shipping address with valid details")`
- `@When("I select a shipping method")`
- `@When("I fill in the payment details with test card")`
- `@When("I accept the terms and conditions")`
- `@When("I click the complete purchase button")`

**Post-Purchase Detection:**
- `@Then("I should be taken to an upsell page or thank you page")`
  - Detects redirect destination
  - Captures order details if on Thank You page

**Upsell Loop:**
- `@And("I navigate through any upsell pages")`
  - Loops through upsells with state tracking
  - Calls helper methods:
    - `addUpsellProductToOrder()` - Adds product to cart
    - `selectUpsellShipping()` - Selects shipping method
    - `acceptAndContinueUpsell()` - Submits upsell
  - Continues until Thank You page or max iterations

**Final Verification:**
- `@Then("I should land on the thank you page")`
- Verifies Thank You page presence
- Calls `captureOrderDetails()` for data extraction

---

### ThankYouPage.java (ENHANCED)
Enhanced with comprehensive order detail extraction:

**Detection Methods:**
- `isThankYouPageDisplayed()` - Detects Thank You page

**Data Extraction Methods:**
- `getOrderNumber()` - Extracts order confirmation #
- `getOrderPrice()` - Extracts total amount
- `getShippingAddress()` - Extracts delivery address
- `getOrderItems()` - Extracts ordered products
- `getOrderSummary()` - Gets full order summary text

**Helper Methods:**
- `getHeading()` - Extracts page heading
- `getOrderConfirmationText()` - Gets body text

---

### UpsellPage.java (ENHANCED)
Comprehensive upsell handling with new methods:

**Page Detection:**
- `isUpsellPage()` - Detects upsell/OTO page
- `isThankYouPage()` - Detects thank you page

**Product Selection (NEW):**
- `addProductToUpsell()` - Selects upsell product
- `getUpsellProductName()` - Gets product details
- `getUpsellPrice()` - Extracts upsell pricing

**Shipping Selection (NEW):**
- `selectUpsellShipping()` - Selects shipping method
- `getAvailableShippingMethods()` - Lists options

**Acceptance & Continuation (ENHANCED):**
- `acceptAndContinue()` - Primary accept method
- `declineOffer()` - Decline option (legacy)

**Navigation:**
- `navigateThroughAllUpsells()` - Legacy decline-based loop

---

## 🧪 Test Scenarios

### Feature File: checkout_flow.feature (UPDATED)

#### Scenario 1: Complete Full Funnel with Multiple Upsells
```gherkin
@order @happy-path @critical
Scenario: Complete full funnel order flow with multiple upsells
  When I select a product on the main page
  And I fill in the shipping address with valid details
  And I select a shipping method
  And I fill in the payment details with test card
  And I accept the terms and conditions
  And I click the complete purchase button
  Then I should be taken to an upsell page or thank you page
  And I navigate through any upsell pages
  Then I should land on the thank you page
```

**Flow:**
1. Complete primary checkout
2. Redirect to upsell OR thank you
3. If upsell: Loop until thank you
4. Verify final order

#### Scenario 2: Direct to Thank You (No Upsells)
```gherkin
@order @direct-thankyou
Scenario: Primary order redirects directly to thank you page
```

**Flow:**
1. Complete primary checkout
2. Direct redirect to thank you
3. Verify order details immediately

#### Scenario 3 & 4: Validation Scenarios
```gherkin
@validation @form-validation
Scenario: Checkout form shows validation errors when submitted empty

@validation @payment-validation
Scenario: Checkout fails with invalid card number
```

---

## 📊 Execution Flow Diagram

```
Start
  ↓
[Primary Checkout Form]
  ├─ Select Product
  ├─ Fill Shipping Address
  ├─ Select Shipping Method
  ├─ Fill Payment Details
  ├─ Accept Terms
  └─ Click Purchase
  ↓
[Order Submission]
  ↓
┌─────────────────────────────────────┐
│  Redirect Detection                 │
│  (verifyPostPurchasePage)           │
└─────────────────────────────────────┘
  │
  ├──→ Thank You Page
  │    ↓
  │    [Capture Order Details]
  │    ├─ Order Number ✓
  │    ├─ Order Price ✓
  │    ├─ Address ✓
  │    └─ Items ✓
  │    ↓
  │    END ✓
  │
  └──→ Upsell Page
       ↓
       [Enter Upsell Loop] (max 10 iterations)
       ↓
┌──────────────────────────────────────┐
│  Loop: For Each Upsell              │
├──────────────────────────────────────┤
│  1. Add Product to Cart             │
│  2. Select Shipping Method          │
│  3. Accept & Continue               │
└──────────────────────────────────────┘
       ↓
┌──────────────────────────────────────┐
│  Redirect Check (after upsell)      │
├──────────────────────────────────────┤
│  ├─ Thank You? → Exit Loop → END ✓ │
│  ├─ Upsell? → Continue Loop         │
│  └─ Other? → Exit Loop              │
└──────────────────────────────────────┘
       ↓
       [Repeat Loop]
```

---

## 🚀 Running the Tests

### Run All Tests
```bash
mvn clean test
```

### Run Smoke Tests Only
```bash
mvn clean test -Dtest=SmokeTestRunner
```

### Run Specific Scenarios
```bash
mvn clean test -Dcucumber.filter.tags="@happy-path"
```

### Run Without Upsells (Direct Thank You)
```bash
mvn clean test -Dcucumber.filter.tags="@direct-thankyou"
```

### Run Validation Tests
```bash
mvn clean test -Dcucumber.filter.tags="@validation"
```

---

## 📝 Test Data

### File: `testdata/checkout_data.json`

```json
{
  "customer": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "johndoe.test@mailinator.com",
    "phone": "5559876543",
    "address": "123 Test Street Austin TX",
    "city": "Austin",
    "state": "Texas",
    "zipCode": "78701",
    "country": "United States"
  },
  "payment": {
    "cardType": "visa",
    "cardNumber": "4111111111111111",
    "expiryMonth": "(01) January",
    "expiryYear": "2030",
    "cvv": "123"
  }
}
```

---

## 🔍 State Tracking

The `CheckoutStepDefs` class maintains state across the funnel flow:

```java
private String originalOrderNumber;      // Captured after primary order
private double originalOrderPrice;       // Captured after primary order
private boolean onThankYouPage = false;  // Tracks if on thank you page
```

This allows:
- Verification that upsell orders reference the same order
- Tracking which page type we're on (upsell vs. thank you)
- Preventing unnecessary processing

---

## 🎯 Key Features

✅ **Complete Funnel Support**
- Handles multiple sequential upsells
- Gracefully handles direct-to-thank-you flow
- Safety limits to prevent infinite loops

✅ **Comprehensive Data Verification**
- Extracts order numbers, pricing, addresses
- Validates all details on thank you page
- Logs all captured data

✅ **Flexible Page Detection**
- Detects pages by URL patterns
- Falls back to DOM element detection
- Supports various page naming conventions

✅ **Detailed Logging**
- ASCII visual separators for clarity
- Step-by-step progress tracking
- Integration with ExtentReports

✅ **Error Handling**
- Try-catch blocks for robustness
- Fallback selectors for variation
- Informative error messages

✅ **BDD Framework**
- Cucumber/Gherkin syntax
- Clear business logic documentation
- Non-technical stakeholder friendly

---

## 📈 Reports

Test execution generates:
- **Cucumber HTML Report**: `reports/cucumber-report.html`
- **Extent Report**: `reports/extent-report.html`
- **Screenshots**: `reports/screenshots/` (on failures)
- **Test Results XML**: For CI/CD integration

---

## 🔄 Maintenance Notes

### Adding a New Upsell Flow
If the actual application adds/changes upsell pages:

1. **Update selectors** in `UpsellPage.java`
   ```java
   private static final String ACCEPT_BTNS = "...your-selectors..."
   ```

2. **Update product selectors** if needed
   ```java
   private void addProductToUpsell() { ... }
   ```

3. **Update shipping selectors** if changed
   ```java
   private void selectUpsellShipping() { ... }
   ```

### Adding Order Details Extraction
To capture additional details on thank you page:

1. **Add method** in `ThankYouPage.java`
   ```java
   public String getAdditionalDetail() { ... }
   ```

2. **Update** `captureOrderDetails()` in `CheckoutStepDefs.java`
   ```java
   String detail = thankYouPage.getAdditionalDetail();
   log("  • Detail: " + detail);
   ```

---

## ⚙️ Configuration

### File: `config.properties`

```properties
# Browser configuration
playwright.headless=true
playwright.browser=chromium
playwright.timeout=30000

# Application URLs
base.url=https://your-funnel-app.com
checkout.path=/checkout

# Reporting
extent.reports.path=./reports
screenshot.on.failure=true
```

---

## 🐛 Debugging Tips

### Enable Trace Debugging
```java
PlaywrightManager.setDebugMode(true);
```

### Increase Timeouts
```java
page.waitForLoadState("networkidle", new Page.WaitForLoadStateOptions().setTimeout(60000));
```

### Check Intermediate Pages
Add screenshots in the loop:
```java
page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("debug_" + i + ".png")));
```

### Print DOM
```java
System.out.println(page.content());
```

---

## 📚 References

- **Playwright Documentation**: https://playwright.dev/java/
- **Cucumber Documentation**: https://cucumber.io/docs/cucumber/
- **Extent Reports**: https://www.extentreports.com/

---

## ✍️ Version History

### v2.0 (Current)
- ✨ **NEW**: Enhanced upsell loop with accept/continue (vs. decline)
- ✨ **NEW**: Product selection in upsells
- ✨ **NEW**: Shipping selection in upsells
- ✨ **NEW**: Comprehensive order detail capture
- 🔧 **IMPROVED**: State tracking across funnel
- 🔧 **IMPROVED**: Detailed logging and visualization
- 🔧 **IMPROVED**: Better page detection logic
- 📝 **UPDATED**: Feature file with business logic documentation

### v1.0
- Initial implementation
- Basic checkout flow
- Decline-based upsell handling
- Simple thank you page validation

---

## 🤝 Support & Contributions

For issues, questions, or feature requests:
1. Check existing test scenarios
2. Review page object implementations
3. Verify test data configuration
4. Enable debug logging for detailed analysis

---

**Last Updated**: February 2025
**Framework**: Playwright + Cucumber + Extent Reports
**Language**: Java