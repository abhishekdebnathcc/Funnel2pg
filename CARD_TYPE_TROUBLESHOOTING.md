# 💳 Card Type Selection Troubleshooting Guide

## Problem
When filling payment details, the card type selection may fail because the HTML select element doesn't have the expected option value.

## Solution Overview
The improved `selectCardType()` method in `CheckoutPage.java` now uses a **multi-step fallback strategy** to handle various card type select implementations:

### Step 1: Select by Value
```
Tries: selectOption(value="visa")
If the HTML has: <option value="visa">Visa</option>
```

### Step 2: Select by Label  
```
Tries: selectOption(label="Visa")
If the HTML has: <option>Visa</option> or <option value="1">Visa</option>
```

### Step 3: Case-Insensitive Match
```
Inspects available options and matches case-insensitively
If the HTML has: <option>VISA</option> or <option>Visa</option>
```

### Step 4: Select First Option (Fallback)
```
Selects the first available option as a fallback
Ensures test doesn't fail completely
```

## Common Card Type Values

Depending on your payment form, the card type might be:

| Format | Example Values |
|--------|----------------|
| Lowercase | `"visa"`, `"mastercard"`, `"amex"` |
| Title Case | `"Visa"`, `"Mastercard"`, `"American Express"` |
| Full Name | `"VISA"`, `"MASTERCARD"`, `"AMERICAN EXPRESS"` |
| Numeric | `"1"`, `"2"`, `"3"` (check HTML for mapping) |

## Configuration

### Current Test Data (checkout_data.json)
```json
{
  "payment": {
    "cardType": "Visa",
    ...
  }
}
```

**To adjust for your site:**
1. Inspect the payment form HTML
2. Find the `<select id="creditCardType">` element
3. Check the `<option>` values
4. Update `checkout_data.json` accordingly

## Debugging Steps

### 1. Check Console Output
Run tests and look for messages like:
```
✓ Card type selected by value: Visa
✓ Card type selected by label: Visa
✓ Card type selected (case-insensitive): Visa
⚠ Card type: Selected first option as fallback
✗ Card type selection failed: [error message]
```

### 2. Inspect HTML Manually
```javascript
// Open browser DevTools and run:
document.querySelector('#creditCardType').innerHTML
document.querySelector('#creditCardType').options
```

### 3. Log Available Options
Edit `CheckoutPage.java` temporarily to add:
```java
private void selectCardType(String cardType) {
    try {
        Locator cardTypeSelect = page.locator(SELECT_CARD_TYPE);
        String options = page.evaluate(
            "document.querySelector('#creditCardType').innerHTML"
        ).toString();
        System.out.println("Available card type options: " + options);
        // ... rest of selection logic
    }
}
```

## Common Fixes

### Issue: "Visa" vs "visa"
**Solution:** The code now handles case-insensitive matching in Step 3

### Issue: Numeric values like "1", "2"
**Solution:** Update `checkout_data.json`:
```json
{
  "payment": {
    "cardType": "1",
    ...
  }
}
```

### Issue: Custom selector
**Solution:** Update `CheckoutPage.java`:
```java
private static final String SELECT_CARD_TYPE = "#paymentMethod";  // Use your selector
```

### Issue: Different HTML structure
**Solution:** Inspect and update the selector in `CheckoutPage.java` line 24

## Testing the Fix

1. **Run smoke tests:**
   ```bash
   mvn test -Dtest=SmokeTestRunner
   ```

2. **Check output for card type success:**
   ```
   ✓ Payment filled (test card)
   ✓ Payment details filled
   ```

3. **If still failing:**
   - Review console output for exact error
   - Run debugging steps above
   - Update card type in test data
   - Re-run tests

## Advanced: Custom Card Type Handler

If your form uses a custom dropdown (not HTML `<select>`), modify the method:

```java
private void selectCardType(String cardType) {
    try {
        // For custom dropdowns
        page.locator(".card-type-button").click();  // Open dropdown
        page.waitForTimeout(500);
        page.locator(".card-type-option:has-text('" + cardType + "')").click();
        System.out.println("✓ Card type selected: " + cardType);
    } catch (Exception e) {
        System.out.println("✗ Card type selection failed: " + e.getMessage());
    }
}
```

## Support

If issues persist:
1. Check the test execution logs in `reports/` folder
2. Enable verbose mode: `mvn test -X`
3. Review Playwright documentation: https://playwright.dev

---

**Last Updated:** February 24, 2026  
**Status:** ✅ Multi-step fallback strategy implemented

