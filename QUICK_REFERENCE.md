# Funnel1pg - Quick Reference Guide

## 🎯 Business Logic at a Glance

```
PRIMARY ORDER SUBMISSION
         ↓
    REDIRECT CHECK
    /            \
THANK YOU     UPSELL
   PAGE        PAGE
   ↓            ↓
  END        ACCEPT & CONTINUE
             ↓
        REDIRECT CHECK
        /            \
    THANK YOU     ANOTHER UPSELL
       PAGE        PAGE
       ↓            ↓
      END        LOOP CONTINUES
                 (max 10 times)
```

---

## 📦 What Gets Verified

### On Primary Order Success
✓ Order is submitted successfully  
✓ Page redirects away from checkout  
✓ Either upsell or thank you page is shown

### On Each Upsell
✓ Product is added to order  
✓ Shipping method is selected  
✓ Upsell is accepted and continued  
✓ System redirects to next page

### On Final Thank You Page
✓ Page is indeed thank you/confirmation  
✓ Order number is present  
✓ Order price/total is displayed  
✓ Shipping address is shown  
✓ Order items are listed  

---

## 🔧 The Three Core Methods

### 1. Click Primary Purchase Button
```java
@When("I click the complete purchase button")
public void clickPurchase() {
    checkoutPage.clickCompletePurchase();
    // Wait for redirect away from /checkout
    page.waitForURL(notCheckout, timeout);
}
```

### 2. Detect Post-Purchase Page Type
```java
@Then("I should be taken to an upsell page or thank you page")
public void verifyPostPurchasePage() {
    if (thankYouPage.isThankYouPageDisplayed()) {
        // Capture and verify order details
        captureOrderDetails();
        return;
    }
    if (upsellPage.isUpsellPage()) {
        // Will enter upsell loop
        return;
    }
}
```

### 3. Process Upsells Until Thank You
```java
@And("I navigate through any upsell pages")
public void navigateUpsells() {
    while (upsellCount < maxUpsells) {
        if (thankYouPage.isThankYouPageDisplayed()) {
            break; // Reached final page
        }
        if (!upsellPage.isUpsellPage()) {
            break; // No more upsells
        }
        
        // Process this upsell
        addUpsellProductToOrder();
        selectUpsellShipping();
        acceptAndContinueUpsell();
        
        // Wait for redirect and loop again
        upsellCount++;
    }
}
```

---

## 📝 Test Data Used

**Customer Info** (from `checkout_data.json`):
- Name: John Doe
- Email: johndoe.test@mailinator.com
- Phone: 555-987-6543
- Address: 123 Test Street Austin TX 78701
- State: Texas

**Payment Info**:
- Card Type: Visa
- Card Number: 4111111111111111 (test card)
- Expiry: January 2030
- CVV: 123

---

## 🚀 Quick Run Commands

### Run All Tests
```bash
mvn clean test
```

### Run Only Happy Path
```bash
mvn clean test -Dcucumber.filter.tags="@happy-path"
```

### Run Only Validation
```bash
mvn clean test -Dcucumber.filter.tags="@validation"
```

### Run Only Direct Thank You (No Upsells)
```bash
mvn clean test -Dcucumber.filter.tags="@direct-thankyou"
```

---

## 📊 Test Report Locations

After running tests, find reports at:

- **Extent Report**: `reports/extent-report.html`
- **Cucumber HTML**: `reports/cucumber-report.html`
- **Failure Screenshots**: `reports/screenshots/`
- **XML Results**: `reports/cucumber-report.xml`

---

## 🔍 Key File Locations

| File | Purpose |
|------|---------|
| `CheckoutStepDefs.java` | Main test orchestration logic |
| `CheckoutPage.java` | Primary checkout form interactions |
| `UpsellPage.java` | Upsell page detection & actions |
| `ThankYouPage.java` | Order confirmation & data extraction |
| `checkout_flow.feature` | BDD test scenarios |
| `checkout_data.json` | Test data (customer, payment) |
| `FUNNEL_IMPLEMENTATION.md` | Detailed documentation |

---

## 🧩 Page Detection Logic

### Upsell Page Detected By:
- URL contains: `upsell`, `oto`, `offer`, `upgrade`
- Has visible accept/decline buttons
- HTML structure matches patterns

### Thank You Page Detected By:
- URL contains: `thank`, `confirm`, `success`, `receipt`
- Has heading: "Thank You", "Order Confirmed", "Success"
- Specific CSS class patterns

---

## 💾 State Variables Tracked

```java
private boolean onThankYouPage = false;      // Page type flag
private String originalOrderNumber;           // Primary order #
private double originalOrderPrice;            // Primary order price
```

This allows proper flow control and prevents redundant processing.

---

## ⚠️ Error Handling

The implementation handles:
- ✓ Missing page elements (try-catch blocks)
- ✓ Timeouts (explicit waits with fallbacks)
- ✓ Network delays (waitForLoadState)
- ✓ Infinite loops (max iteration limit = 10)
- ✓ Missing order details (returns "Not found")

---

## 🎨 Logging Format

Visual indicators in console output:
- `✓` - Success
- `✗` - Failure
- `→` - Progress/Flow
- `ℹ` - Information
- `⚠` - Warning
- `📄` - Document/Page
- `📦` - Order/Shipping
- `🔍` - Detection/Search
- `🛒` - Shopping/Cart
- `📋` - Verification/Details
- `✅` - Completion

---

## 🔄 The Upsell Loop in Detail

Each iteration:

```
1. Check if Thank You page → EXIT LOOP
2. Check if Upsell page → CONTINUE
3. Add product to order
4. Select shipping method
5. Accept & continue
6. Wait for redirect
7. Count++
8. REPEAT from step 1
```

Exits when:
- ✓ Thank You page reached
- ✓ No upsell page detected
- ✓ Max iterations (10) reached

---

## 📈 Order Detail Extraction

On Thank You page, the test captures:

| Detail | Method | Found In |
|--------|--------|----------|
| Order # | `getOrderNumber()` | Order confirmation section |
| Total Price | `getOrderPrice()` | Price/amount displays |
| Shipping Addr | `getShippingAddress()` | Address section |
| Items | `getOrderItems()` | Line items/products list |
| Summary | `getOrderSummary()` | Full confirmation text |

---

## 🧪 Scenario Coverage

### Happy Path (Complete Funnel)
- ✓ Product selection
- ✓ Checkout form completion
- ✓ Payment processing
- ✓ Primary order submission
- ✓ Upsell detection
- ✓ Multiple upsell handling
- ✓ Thank you page verification
- ✓ Order detail validation

### Direct Path (No Upsells)
- ✓ Primary order → Direct to thank you
- ✓ Verify all details immediately

### Validation
- ✓ Empty form submission (errors expected)
- ✓ Invalid card processing (errors expected)

---

## 💡 Pro Tips

1. **Debug Selector Issues**
   - Check browser dev tools for actual element IDs/classes
   - Update selectors in page object classes
   - Test selectors in browser console

2. **Extend for More Scenarios**
   - Add new @When/@Then methods
   - Create new page objects as needed
   - Update feature file with new scenarios

3. **Adjust Timeouts**
   - Increase for slow environments
   - Decrease for faster testing
   - Set in `config.properties`

4. **Check Test Data**
   - Ensure checkout_data.json has valid values
   - Update email if single-use test accounts
   - Verify card numbers still work

---

## 🆘 Troubleshooting

### Test Hangs on Upsell Loop
- Check max iteration limit (should be 10)
- Verify upsell page detection logic
- Check if thank you page selector needs update

### Order Details Not Captured
- Inspect actual thank you page HTML
- Update selectors in `ThankYouPage.java`
- Try different element locator strategies

### Payment Error on Valid Card
- Verify card number in test data
- Check card expiry date
- Ensure CVV is correct

### Timeout Waiting for Redirect
- Increase timeout value (currently 20s)
- Check if page is loading properly
- Verify network connectivity

---

## 📚 Further Reading

- See `FUNNEL_IMPLEMENTATION.md` for full documentation
- Check `checkout_flow.feature` for all test scenarios
- Review page object classes for selector details
- Check inline code comments for specific logic

---

**Last Updated**: February 2025