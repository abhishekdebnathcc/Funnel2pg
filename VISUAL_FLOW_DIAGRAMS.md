# Funnel1pg - Visual Flow Diagrams & Architecture

## 🎯 Complete Funnel Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     FUNNEL1PG CHECKOUT FLOW                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│                         START: Checkout Page                                 │
│                                  │                                           │
│                                  ▼                                           │
│                      ┌───────────────────────┐                              │
│                      │  Select Product       │                              │
│                      │  Fill Shipping Info   │  ◄─── CheckoutStepDefs     │
│                      │  Select Shipping      │  ◄─── CheckoutPage         │
│                      │  Fill Payment Details │                              │
│                      │  Accept Terms         │                              │
│                      └───────────────────────┘                              │
│                                  │                                           │
│                                  ▼                                           │
│                      ┌───────────────────────┐                              │
│                      │  SUBMIT PRIMARY ORDER │ ◄─── @When step            │
│                      └───────────────────────┘                              │
│                                  │                                           │
│                      ────────────┴────────────                              │
│                      │                       │                              │
│                   SUCCESS              FAILURE                              │
│                      │                       │                              │
│                      ▼                       ▼                              │
│              ┌──────────────┐        ┌──────────────┐                      │
│              │ Page Redirect│        │  Error Page  │                      │
│              │ Wait...      │        │  (Test Fails)│                      │
│              └──────────────┘        └──────────────┘                      │
│                      │                                                       │
│        ┌─────────────┴─────────────┐                                        │
│        │                           │                                        │
│        ▼                           ▼                                        │
│   ┌─────────────┐            ┌──────────────┐                              │
│   │  THANK YOU  │            │   UPSELL     │ ◄─── @Then step             │
│   │    PAGE     │            │     PAGE     │                              │
│   └─────────────┘            └──────────────┘                              │
│        │                            │                                       │
│        │                            ▼                                       │
│        │                   ┌──────────────────────┐                        │
│        │                   │ UPSELL LOOP STARTS   │ ◄─── @And step        │
│        │                   │ (max 10 iterations)  │                        │
│        │                   └──────────────────────┘                        │
│        │                            │                                       │
│        │                            ▼                                       │
│        │                   ┌──────────────────────┐                        │
│        │                   │ [ITERATION N]        │                        │
│        │                   │                      │ ◄─── UpsellPage       │
│        │                   │ 1. Add Product       │                        │
│        │                   │ 2. Select Shipping   │                        │
│        │                   │ 3. Accept & Continue │                        │
│        │                   │                      │                        │
│        │                   └──────────────────────┘                        │
│        │                            │                                       │
│        │                            ▼                                       │
│        │                   ┌──────────────────────┐                        │
│        │                   │ WAIT FOR REDIRECT    │                        │
│        │                   │ ...                  │                        │
│        │                   └──────────────────────┘                        │
│        │                            │                                       │
│        │                ┌───────────┴───────────┐                          │
│        │                │                       │                          │
│        │                ▼                       ▼                          │
│        │          ┌──────────────┐      ┌──────────────┐                  │
│        │          │  THANK YOU   │      │   UPSELL     │                  │
│        │          │    PAGE      │      │     PAGE     │                  │
│        │          │   REACHED    │      │ (Continue    │                  │
│        │          │              │      │  Loop)       │                  │
│        │          │   EXIT LOOP  │      │              │                  │
│        │          │      ↓       │      │   N++        │                  │
│        │          │              │      │      ↓       │                  │
│        │          └──────────────┘      │    LOOP      │                  │
│        │                │                │   AGAIN      │                  │
│        │                │                └──────────────┘                  │
│        │                │                       │                          │
│        │                │◄──────────────────────┘                          │
│        │                │                                                   │
│        └────────────────┼─────────────────────────────────────────────────┘│
│                         │                                                  │
│                         ▼                                                  │
│                ┌──────────────────────────┐                               │
│                │  THANK YOU PAGE REACHED  │ ◄─── @Then step (final)      │
│                │  (Exit all loops)        │                               │
│                └──────────────────────────┘                               │
│                         │                                                  │
│                         ▼                                                  │
│                ┌──────────────────────────┐                               │
│                │ VERIFY ORDER DETAILS:    │                               │
│                │ ✓ Order Number          │ ◄─── ThankYouPage           │
│                │ ✓ Order Total Price     │                               │
│                │ ✓ Shipping Address      │                               │
│                │ ✓ Order Items           │                               │
│                │ ✓ Summary Text          │                               │
│                └──────────────────────────┘                               │
│                         │                                                  │
│                         ▼                                                  │
│                      ✅ TEST COMPLETE                                      │
│                                                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Class Architecture Diagram

```
┌────────────────────────────────────────────────────────────────┐
│                    TEST ORCHESTRATION LAYER                    │
│                                                                │
│                    CheckoutStepDefs.java                      │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Main Test Execution Engine                              │ │
│  │                                                          │ │
│  │ • navigateToCheckout()                                  │ │
│  │ • selectProduct()                                       │ │
│  │ • fillShippingAddress()                                 │ │
│  │ • fillPaymentDetails()                                  │ │
│  │ • clickPurchase()                                       │ │
│  │ • verifyPostPurchasePage()                              │ │
│  │ • navigateUpsells()  ◄─── MAIN LOOP ORCHESTRATOR       │ │
│  │ • verifyThankYouPage()                                  │ │
│  │ • captureOrderDetails()                                 │ │
│  │                                                          │ │
│  │ State Variables:                                        │ │
│  │ • onThankYouPage (boolean)                             │ │
│  │ • originalOrderNumber (String)                         │ │
│  │ • originalOrderPrice (double)                          │ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
         │                      │                     │
         ▼                      ▼                     ▼
    ┌─────────────┐      ┌──────────────┐    ┌──────────────┐
    │ CheckoutPage│      │ UpsellPage   │    │ ThankYouPage │
    │   (Page 1)  │      │   (Page 2)   │    │   (Page 3)   │
    └─────────────┘      └──────────────┘    └──────────────┘
         │                      │                     │
         ▼                      ▼                     ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌──────────────────┐
    │PRIMARY CHECKOUT │ │UPSELL HANDLING  │ │ORDER VERIFICATION│
    ├─────────────────┤ ├─────────────────┤ ├──────────────────┤
    │ Locators:       │ │ Locators:       │ │ Locators:        │
    │ • .sec-btn1     │ │ • ACCEPT_BTNS   │ │ • [class*=order] │
    │ • #inputFN      │ │ • DECLINE_BTNS  │ │ • [class*=price] │
    │ • #inputLN      │ │ • #ship-methods │ │ • [class*=addr]  │
    │ • #inputAddr    │ │ • #continue-btn │ │ • h1, h2         │
    │ • #ccNumber     │ │                 │ │ • [class*=total] │
    │ • #cvv          │ │ Methods:        │ │                  │
    │ • #terms-cb     │ │ • isUpsellPage()│ │ Methods:         │
    │                 │ │ • addProduct()  │ │ • getOrderNum()  │
    │ Methods:        │ │ • selectShip()  │ │ • getOrderPrice()│
    │ • selectProd()  │ │ • accept&Cont() │ │ • getAddress()   │
    │ • fillAddress() │ │ • isThankYou()  │ │ • getItems()     │
    │ • fillPayment() │ │                 │ │ • getSummary()   │
    │ • hasErrors()   │ │                 │ │                  │
    └─────────────────┘ └─────────────────┘ └──────────────────┘
         │                      │                     │
         └──────────────────────┴─────────────────────┘
                       │
                       ▼
              ┌──────────────────┐
              │   BasePage.java  │
              ├──────────────────┤
              │ Common Methods:  │
              │ • getCurrentUrl()│
              │ • safeFill()     │
              │ • click()        │
              │ • waitForVisible()
              │ • selectOption() │
              └──────────────────┘
                       │
                       ▼
              ┌──────────────────┐
              │    Page Object   │
              │ (Playwright API) │
              └──────────────────┘
```

---

## 🔄 Upsell Loop State Machine

```
┌─────────────────────────────────────────────────────────────┐
│              UPSELL LOOP STATE MACHINE                       │
└─────────────────────────────────────────────────────────────┘

    ╔════════════════════════════════╗
    ║  ENTER UPSELL LOOP             ║
    ║  Count = 0                     ║
    ║  MaxCount = 10                 ║
    ╚════════════════════════════════╝
              │
              ▼
    ╔════════════════════════════════╗         ┌──────────────┐
    ║  CHECK: Count < MaxCount?      ║───NO──→ │ EXIT LOOP    │
    ╚════════════════════════════════╝         │ (Max Reached)│
              │ YES                             └──────────────┘
              ▼
    ╔════════════════════════════════╗
    ║  PAGE DETECTION                ║
    ║  • Check current URL           ║
    ║  • Check for page elements     ║
    ╚════════════════════════════════╝
              │
      ┌───────┴──────────┐
      │                  │
      ▼                  ▼
    THANK YOU        UPSELL          OTHER
     PAGE            PAGE            PAGE
      │                │              │
      │                ▼              ▼
      │        ╔════════════════╗   EXIT
      │        ║ ADD PRODUCT    ║   LOOP
      │        ║ SELECT SHIPPING║
      │        ║ ACCEPT & CONT. ║
      │        ╚════════════════╝
      │                │
      │                ▼
      │        ╔════════════════╗
      │        ║ WAIT REDIRECT  ║
      │        ║ (timeout 15s)  ║
      │        ╚════════════════╝
      │                │
      │                ▼
      │        ╔════════════════╗
      │        ║ Count++        ║
      │        ║ LOOP AGAIN     ║
      │        ╚════════════════╝
      │                │
      └────────────────┼────────────┐
                       │            │
                  [Loop Again]      │
                       │            │
                       └────────────┘
                            │
                            ▼
                       ✅ LOOP EXIT
```

---

## 📊 State Tracking Diagram

```
┌─────────────────────────────────────────────────────────┐
│                  STATE VARIABLES                        │
│                                                         │
│  onThankYouPage (boolean)                              │
│  ├─ Initial: false                                      │
│  ├─ Set to: true when thank you page detected          │
│  ├─ Used by: navigateUpsells() to exit loop           │
│  └─ Purpose: Track if we've reached final page        │
│                                                         │
│  originalOrderNumber (String)                          │
│  ├─ Captured: From primary order submit                │
│  ├─ Used by: Order verification                       │
│  └─ Purpose: Compare with upsell orders (if needed)   │
│                                                         │
│  originalOrderPrice (double)                           │
│  ├─ Captured: From primary order submit                │
│  ├─ Used by: Price tracking across funnel             │
│  └─ Purpose: Verify total includes all upsells       │
│                                                         │
└─────────────────────────────────────────────────────────┘

           Flow of State Changes:
           
   START
    │
    ├─ onThankYouPage = false
    ├─ originalOrderNumber = null
    ├─ originalOrderPrice = 0.0
    │
    ▼
   PRIMARY ORDER SUBMITTED
    │
    ├─ Detect page type
    │
    ├─→ If Thank You:
    │   ├─ onThankYouPage = true
    │   ├─ Capture order details
    │   └─ Exit
    │
    └─→ If Upsell:
        ├─ onThankYouPage = false
        ├─ Enter loop
        │
        └─→ In Loop:
            ├─ Process upsell
            │
            ├─→ If Thank You reached:
            │   ├─ onThankYouPage = true
            │   ├─ Exit loop
            │   ├─ Capture order details
            │   └─ END
            │
            └─→ If Another Upsell:
                └─ Continue loop
```

---

## 📈 Data Flow from Checkout to Thank You

```
CHECKOUT PAGE                    UPSELL PAGES              THANK YOU PAGE
─────────────                    ────────────              ──────────────

Customer Data                    (Retained)                 Order Details
├─ Name              ──────────┐                           ├─ Order #
├─ Email             ──────────┼──────────────────────────→├─ Total Price
├─ Phone             ──────────┤                           ├─ Address
├─ Address           ──────────┘                           ├─ Items List
├─ Shipping Method   ──────────┐                           ├─ Confirmation
│                               │                           │  Text
Payment Data                    │ Upsell Products         │
├─ Card Type         ──────────┼─ Product 1              │ Extracted Data
├─ Card Number       ──────────┤─ Product 2              │ ├─ getOrderNum()
├─ Expiry            ──────────┤─ Product 3              │ ├─ getOrderPrice()
├─ CVV               ──────────┘─ ...                    │ ├─ getAddress()
│                               │ Shipping Options        │ ├─ getItems()
Primary Order        Primary +  │ Selected               │ └─ getSummary()
├─ Order Number      ├──────────┼─ Shipping              │
├─ Order Total       │ Upsell 1  │                        ├─→ CAPTURED &
└─ Order Items       │ Upsell 2  │                        │   VERIFIED
                     │ Upsell 3  │                        │
                     │ ...       │                        └─ REPORTED
                     └───────────┘
```

---

## 🧪 Test Data Flow

```
CONFIGURATION
│
├─ BaseURL
│  └─ https://your-funnel-app.com
│
├─ Selectors
│  ├─ CheckoutPage: Input fields, buttons
│  ├─ UpsellPage: Accept/Decline buttons
│  └─ ThankYouPage: Confirmation elements
│
└─ Browser Settings
   ├─ Headless: true/false
   ├─ Timeout: 30000ms
   └─ Browser: Chromium

                    ▼

TEST DATA FILE (checkout_data.json)
│
├─ Customer
│  ├─ firstName: "John"
│  ├─ lastName: "Doe"
│  ├─ email: "johndoe.test@mailinator.com"
│  ├─ phone: "5559876543"
│  ├─ address: "123 Test Street Austin TX"
│  ├─ city: "Austin"
│  ├─ state: "Texas"
│  ├─ zipCode: "78701"
│  └─ country: "United States"
│
└─ Payment
   ├─ cardType: "visa"
   ├─ cardNumber: "4111111111111111"
   ├─ expiryMonth: "(01) January"
   ├─ expiryYear: "2030"
   └─ cvv: "123"

                    ▼

LOADED BY TestDataReader
│
├─ getCustomer(field)
│  └─ Returns: Customer data values
│
└─ getPayment(field)
   └─ Returns: Payment data values

                    ▼

USED IN TEST EXECUTION
│
├─ CheckoutStepDefs
│  ├─ fillShippingAddress()
│  │  └─ Uses: Customer data
│  │
│  └─ fillPaymentDetails()
│     └─ Uses: Payment data
│
└─ Feature Scenarios
   └─ Uses: All data via step definitions
```

---

## 🎯 Selector Strategy Hierarchy

```
For Finding Elements:

LEVEL 1: Specific ID/Class
├─ #inputFirstName[name='firstName']
├─ #creditCardType
├─ #terms-conditions
└─ .sec-btn1

                    │
                    ▼ (If not found)

LEVEL 2: Pattern Matching
├─ button:has-text('COMPLETE YOUR SECURE PURCHASE')
├─ button:has-text('Continue')
├─ span:has-text('Order')
└─ div:has-text('Total')

                    │
                    ▼ (If not found)

LEVEL 3: Class Pattern
├─ [class*='error']
├─ [class*='order-number']
├─ [class*='thank']
└─ [class*='upsell']

                    │
                    ▼ (If not found)

LEVEL 4: Generic Element
├─ h1, h2 (for headings)
├─ button (first available)
├─ input[type='text']
└─ input[type='radio']

                    │
                    ▼ (If not found)

FALLBACK: Return "Not found"
└─ With error logging
```

---

## 📝 Report Generation Flow

```
TEST EXECUTION
│
└─ Logging Statements
   ├─ Console Output
   │  └─ System.out.println()
   │     ├─ ✓ Success indicators
   │     ├─ ✗ Failure indicators
   │     ├─ → Flow progress
   │     └─ ℹ Information
   │
   └─ Extent Report Logging
      └─ ExtentReportManager.getTest()
         ├─ log(Status.PASS, "message")
         ├─ log(Status.INFO, "message")
         └─ log(Status.WARNING, "message")

                    │
                    ▼

SCREENSHOT CAPTURE
│
├─ On Failure
│  └─ ScreenshotUtil.takeScreenshot()
│     └─ Saved to: reports/screenshots/
│
└─ On Assertion
   └─ Captured for evidence

                    │
                    ▼

REPORT GENERATION
│
├─ Extent Report
│  └─ reports/extent-report.html
│     ├─ Test execution timeline
│     ├─ Pass/Fail summary
│     ├─ Screenshots attached
│     └─ Detailed logs
│
├─ Cucumber Report
│  └─ reports/cucumber-report.html
│     ├─ Scenario results
│     ├─ Step definitions
│     └─ Feature overview
│
└─ Test Result XML
   └─ reports/cucumber-report.xml
      └─ For CI/CD integration
```

---

## 🔐 Error Handling Flow

```
TRY BLOCK
│
├─ Attempt Action
│  ├─ Fill field
│  ├─ Click button
│  ├─ Select option
│  └─ Wait for element
│
└─→ Success?
    │
    ├─ YES: Continue ✓
    │
    └─ NO: CATCH EXCEPTION
           │
           ├─ Log Error
           │  └─ System.out.println()
           │
           ├─ Try Fallback Selector
           │  └─ Alternative selector attempt
           │
           └─→ Success?
               ├─ YES: Continue ✓
               └─ NO: Continue with graceful degradation
```

---

**Last Updated**: February 2025