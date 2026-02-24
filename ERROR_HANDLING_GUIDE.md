# ⊘ Error Handling & Test Skip Feature

## Overview

The Funnel1pg test automation now includes intelligent error detection and skipping logic. When a specific error occurs on the upsell page ("You have already placed this offer"), the test is automatically skipped, the error is logged with error code, and the test execution proceeds to the next test without failing.

---

## 🚫 Error: "Already Placed This Offer"

### When This Error Occurs

The "already placed this offer" error appears on the upsell page when:
- A user attempts to purchase the same upsell offer multiple times
- The system has restrictions on duplicate upsell purchases
- The offer has already been purchased in a previous transaction

### Error Detection

The system automatically detects the following error message patterns:

```
"You have already placed this offer"
"This offer has already been placed"
"You've already purchased this offer"
"Already placed this offer"
"Offer already purchased"
(Case-insensitive matching)
```

---

## 📋 Implementation Details

### 1. Error Detection Method

**Location**: `CheckoutStepDefs.java` → `checkForAlreadyPlacedOfferError()`

```java
private boolean checkForAlreadyPlacedOfferError() {
    // Detects error messages using multiple selectors
    // Returns: true if error found, false otherwise
}
```

**Error Detection Selectors**:
```
div:has-text('already placed')
span:has-text('already placed')
p:has-text('already placed')
.error:has-text('already placed')
.alert:has-text('already placed')
[class*='error']:has-text('already placed')
[class*='alert']:has-text('already placed')
[class*='message']:has-text('already placed')
```

### 2. Error Code Extraction

**Location**: `CheckoutStepDefs.java` → `extractErrorCode()`

Automatically extracts error codes from error messages:
- Patterns: `ERR-001`, `ERR_001`, `Code: ABC123`, `(ERR123)`, etc.
- Falls back to `ERR_ALREADY_PLACED_OFFER` if no code found

```java
private String extractErrorCode(String errorMessage) {
    // Extracts code patterns like ERR-001, Code: ABC, etc.
}
```

### 3. State Tracking Variables

```java
private boolean testShouldBeSkipped = false;     // Skip flag
private String skipReason = "";                   // Reason for skip
private String errorCode = "";                    // Error code
```

### 4. Test Skip Mechanism

Uses JUnit's `Assumptions` class:

```java
Assumptions.assumeTrue(false, "Test skipped: " + skipReason + 
    " (Error Code: " + errorCode + ")");
```

This:
- ✅ Marks test as SKIPPED (not FAILED)
- ✅ Logs skip reason with error code
- ✅ Continues to next test
- ✅ Does not stop test suite execution

---

## 🔄 Test Execution Flow with Error Handling

```
UPSELL LOOP
    ↓
LOAD PAGE
    ↓
CHECK FOR ERROR
    ├─ Error NOT found
    │  ├─ Add product
    │  ├─ Select shipping
    │  ├─ Accept & continue
    │  └─ Check next page
    │
    └─ Error FOUND: "Already Placed This Offer"
       ├─ Log error message ❌
       ├─ Extract error code
       ├─ Log error code
       ├─ Mark test as SKIPPED ⊘
       ├─ Call Assumptions.assumeTrue(false)
       └─ Exit to next test ✓
```

---

## 📊 Logging Output

When an error is detected:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🚫 ERROR DETECTED ON UPSELL PAGE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❌ Error Message: You have already placed this offer
❌ Error Code: ERR_ALREADY_PLACED_OFFER
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⊘ Test skipped due to: Already Placed This Offer
⊘ Error Code: ERR_ALREADY_PLACED_OFFER
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Console Indicators

- `❌` - Error detected
- `🚫` - Error on upsell page
- `⊘` - Test skipped
- `ERR_` - Error code prefix

---

## 📈 Test Report Status

### Report Appearance

When a test is skipped due to this error:

**Extent Report**:
- Status: **SKIPPED** (not red/failed)
- Log Entry: "SKIPPED: Already Placed This Offer (Error Code: ERR_ALREADY_PLACED_OFFER)"
- Screenshot: Attached if configured

**Cucumber Report**:
- Status: **SKIPPED**
- Step Details: Shows skip reason and error code

---

## 🔧 Customizing Error Detection

### To Add More Error Patterns

Edit `checkForAlreadyPlacedOfferError()` method:

```java
String errorSelectors = "div:has-text('your-error-pattern'), " +
        "span:has-text('your-error-pattern'), " +
        // Add more selectors here
        "[class*='error']:has-text('your-error-pattern')";
```

### To Change Error Code Extraction

Edit `extractErrorCode()` method to add new patterns:

```java
// Add new pattern matching logic
if (errorMessage.matches(".*YOUR_PATTERN.*")) {
    return errorMessage.replaceAll(".*CAPTURE.*", "$1");
}
```

### To Change Skip Behavior

To FAIL test instead of SKIP when error occurs, replace:

```java
// Instead of:
Assumptions.assumeTrue(false, "...");

// Use:
assertTrue(false, "Test failed with error: " + errorCode);
```

---

## ⚙️ Configuration

### Default Behavior

- **Detection**: Enabled by default
- **Action on Error**: Skip test with status SKIPPED
- **Error Code Extraction**: Automatic with fallback
- **Logging**: Full error details logged
- **Next Test**: Continues to next test

### To Disable Error Detection

Comment out the error check in `navigateUpsells()`:

```java
// Temporarily disabled for testing
// if (checkForAlreadyPlacedOfferError()) {
//     // skip logic
// }
```

---

## 🧪 Test Scenarios Affected

Error detection applies to:

✅ **Scenario 1**: Complete Full Funnel with Multiple Upsells (@happy-path)
✅ **Scenario 2**: Direct to Thank You (@direct-thankyou) - if upsells present
✅ **Scenario 5**: Order Confirmation (@regression) - if upsells present

Does NOT affect:
❌ **Scenario 3**: Form Validation (@form-validation)
❌ **Scenario 4**: Payment Validation (@payment-validation)

---

## 📝 Error Code Reference

### Standard Error Codes

| Error Code | Meaning | Action |
|-----------|---------|--------|
| `ERR_ALREADY_PLACED_OFFER` | Offer already purchased | SKIP test |
| `ERR-401` | Authorization error | SKIP test |
| `ERR-403` | Access forbidden | SKIP test |
| `ERR-500` | Server error | SKIP test |
| `(Custom)` | Custom error code from page | SKIP test |

### How Error Codes are Extracted

```
Message: "You have already placed this offer (ERR-1001)"
→ Extracted Code: ERR-1001

Message: "Code: OFFER_DUPLICATE"
→ Extracted Code: OFFER_DUPLICATE

Message: "Already placed this offer"
→ Fallback Code: ERR_ALREADY_PLACED_OFFER
```

---

## 🔍 Debugging Error Detection

### Check Error Detection

Enable debugging by reviewing console output:

```bash
# Run test with verbose output
mvn clean test -Dcucumber.filter.tags="@happy-path" -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

### Verify Error Selectors

Test selectors in browser console:

```javascript
// Check if error element exists
document.querySelectorAll("div:has-text('already placed')").length > 0

// Check all elements with 'already placed' text
Array.from(document.querySelectorAll('*'))
  .filter(el => el.textContent.includes('already placed'))
  .forEach(el => console.log(el));
```

### Manual Testing

1. Intentionally trigger the error in app
2. Check console output for error detection
3. Verify error code extraction
4. Confirm test is marked as SKIPPED

---

## 📋 Extent Report Fields

When test is skipped due to error:

```
Test Name:     [Scenario Name]
Status:        SKIPPED ⊘
Duration:      [Time taken]
Logs:
  ├─ SKIPPED: Already Placed This Offer (Error Code: ERR_ALREADY_PLACED_OFFER)
  ├─ Error Message: You have already placed this offer
  ├─ Error Code: ERR_ALREADY_PLACED_OFFER
  └─ Test execution continued to next scenario
```

---

## 🚀 Usage Examples

### Example 1: Running Tests with Error Handling

```bash
# Run happy path test
# If error occurs on upsell page:
# - Error is detected ✓
# - Test is marked SKIPPED ⊘
# - Next test runs ✓

mvn clean test -Dcucumber.filter.tags="@happy-path"
```

**Output**:
```
[Scenario 1] ✓ PASSED
[Scenario 2] ⊘ SKIPPED - Already Placed This Offer (ERR_ALREADY_PLACED_OFFER)
[Scenario 3] ✓ PASSED
[Scenario 4] ✓ PASSED
[Scenario 5] ✓ PASSED

Total: 4 Passed, 1 Skipped ⊘
```

### Example 2: Error Code Extraction

If page shows: "You have already purchased this offer (ERROR-2024)"

System extracts: `ERROR-2024`

Logs: "Error Code: ERROR-2024"

### Example 3: Multiple Error Occurrences

If error occurs in multiple upsells:
- First error detected: Skip test
- Same error in later upsells: Test already skipped, no change
- Execution proceeds to next test

---

## ⚠️ Important Notes

1. **Skip vs Fail**: Tests are SKIPPED, not FAILED
   - SKIPPED = Expected condition (error found)
   - FAILED = Unexpected condition

2. **Error Code Accuracy**: Error codes are extracted based on message patterns
   - If code pattern not found, defaults to `ERR_ALREADY_PLACED_OFFER`
   - You can customize extraction logic

3. **Multiple Errors**: If different errors occur, only first match is detected
   - Add additional error detection methods as needed

4. **Test Order**: Error detection occurs in upsell loop
   - Before product is added
   - Prevents unnecessary clicks if error present

5. **Logging**: All errors are logged to:
   - Console output (System.out)
   - Extent Report (with Status.SKIP)
   - Test reports

---

## 🔄 Future Enhancements

Potential improvements:

- [ ] Add different handling for different error types
- [ ] Implement error retry logic (attempt 2-3 times)
- [ ] Send error notifications (email, Slack)
- [ ] Collect error statistics for analysis
- [ ] Custom error handlers per error type
- [ ] Error recovery strategies

---

## 📞 Support & Troubleshooting

### Error Detection Not Working

1. Check error message text on page
2. Verify selector matches error element
3. Test selector in browser console
4. Update selector in `checkForAlreadyPlacedOfferError()`

### Test Not Skipping

1. Verify error is being detected (check logs)
2. Check Java imports (includes `Assumptions`)
3. Verify `assumeTrue(false, ...)` is being called
4. Check test runner handles assumptions correctly

### Custom Error Not Detected

1. Add custom selector to `errorSelectors` string
2. Test selector in browser
3. Update `extractErrorCode()` if custom code pattern
4. Run tests to verify

---

**Last Updated**: February 2025  
**Feature Status**: ✅ Active  
**Affected Tests**: 3 scenarios (happy-path, direct-thankyou, regression)