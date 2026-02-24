# 🔍 Error Detection - Exact Error Message Update

## Updated Error Message

**Old**: "You have already placed this offer"  
**New (Exact)**: "You have already purchased this trial offer"

---

## Error Detection Selectors

The system now detects these exact patterns:

### Primary Selectors (Exact Match)
```
div:has-text('You have already purchased this trial offer')
span:has-text('You have already purchased this trial offer')
p:has-text('You have already purchased this trial offer')
h1:has-text('You have already purchased this trial offer')
h2:has-text('You have already purchased this trial offer')
```

### Secondary Selectors (Partial Match)
```
div:has-text('already purchased this trial')
span:has-text('already purchased this trial')
p:has-text('already purchased this trial')
```

### Container Selectors (Error/Alert Elements)
```
.error:has-text('already purchased')
.alert:has-text('already purchased')
.warning:has-text('already purchased')
[class*='error']:has-text('already purchased')
[class*='alert']:has-text('already purchased')
[class*='message']:has-text('already purchased')
[class*='notification']:has-text('already purchased')
```

---

## Error Code

**Default Error Code**: `ERR_TRIAL_OFFER_ALREADY_PURCHASED`

If a specific error code is embedded in the message, it will be extracted automatically.

---

## Skip Reason

**Skip Reason**: "Trial Offer Already Purchased"

---

## Console Output

When error is detected:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🚫 ERROR DETECTED ON UPSELL PAGE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❌ Error Message: You have already purchased this trial offer
❌ Error Code: ERR_TRIAL_OFFER_ALREADY_PURCHASED
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⊘ Test skipped due to: Trial Offer Already Purchased
⊘ Error Code: ERR_TRIAL_OFFER_ALREADY_PURCHASED
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## Report Status

**Test Status**: SKIPPED ⊘  
**Log Entry**: "SKIPPED: Trial Offer Already Purchased (Error Code: ERR_TRIAL_OFFER_ALREADY_PURCHASED)"

---

## Test Execution Flow

1. Primary order submitted
2. Redirected to upsell page
3. Error detection check:
   - Looks for "You have already purchased this trial offer"
   - If found: Skip test, log error code, continue to next test
   - If not found: Process upsell normally

---

## Implementation Location

**File**: `src/test/java/com/funnel1pg/stepdefs/CheckoutStepDefs.java`  
**Method**: `checkForAlreadyPlacedOfferError()`  
**Lines**: 285-330

---

**Status**: ✅ Updated with exact error message