# 🧪 Test Execution & Verification Guide

## ✅ Project Setup Verification

All components are properly configured:

### ✓ Configuration Files
- **config.properties**: ✅ Present and configured
  - Base URL: `https://stagingabhishek.gupigayen.com/1pgCC23Feb/`
  - Browser: `chromium`
  - Headless: `false`
  - Timeout: `10000ms`
  
### ✓ Java & Build Setup
- **Java Version**: OpenJDK 21.0.10
- **Maven POM**: ✅ Configured
- **Dependencies**: ✅ All required (Playwright, Cucumber, JUnit, ExtentReports)

### ✓ Code Files
- **CheckoutStepDefs.java**: ✅ 389 lines (enhanced)
- **UpsellPage.java**: ✅ 283 lines (enhanced)
- **ThankYouPage.java**: ✅ 193 lines (enhanced)
- **checkout_flow.feature**: ✅ 115 lines (updated)

### ✓ Test Infrastructure
- **Hooks.java**: ✅ Browser lifecycle management
- **PlaywrightManager.java**: ✅ Playwright initialization
- **ConfigReader.java**: ✅ Configuration loading
- **ExtentReportManager.java**: ✅ Report generation
- **Test Runners**: ✅ SmokeTestRunner, RegressionTestRunner, ValidationTestRunner

### ✓ Documentation
- **QUICK_REFERENCE.md**: ✅ 336 lines
- **IMPLEMENTATION_SUMMARY.md**: ✅ 484 lines
- **FUNNEL_IMPLEMENTATION.md**: ✅ 518 lines
- **VISUAL_FLOW_DIAGRAMS.md**: ✅ 538 lines
- **README_CHANGES.md**: ✅ 225 lines
- **DOCUMENTATION_INDEX.md**: ✅ 374 lines

---

## 🚀 How to Run Tests (From Your Machine)

Since you're on macOS and the project is at `/Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/`:

### Prerequisites
```bash
# 1. Ensure Maven is installed
brew install maven  # if not already installed

# 2. Ensure Java 11+ is installed
java -version

# 3. Go to project directory
cd /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/
```

### Run Commands

#### 1. **Happy Path Test (Primary Scenario)**
```bash
mvn clean test -Dcucumber.filter.tags="@happy-path"
```
**What it tests**: Complete funnel flow with multiple upsells
**Expected Result**: All steps pass, Thank You page verified

#### 2. **All Tests**
```bash
mvn clean test
```
**What it tests**: All scenarios (happy path, direct thank you, validation)
**Expected Result**: All tests pass

#### 3. **Direct to Thank You (No Upsells)**
```bash
mvn clean test -Dcucumber.filter.tags="@direct-thankyou"
```
**What it tests**: Primary order directly to thank you page
**Expected Result**: Order verified on thank you page

#### 4. **Validation Tests Only**
```bash
mvn clean test -Dcucumber.filter.tags="@validation"
```
**What it tests**: Form validation and payment errors
**Expected Result**: Error messages displayed correctly

#### 5. **Specific Runner**
```bash
mvn clean test -Drunner=SmokeTestRunner
```

---

## 📊 Test Reports Location

After running tests, find reports at:

```
/Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/

├── extent-report.html          ← Full detailed report (OPEN THIS!)
├── cucumber-report.html        ← Cucumber BDD report
├── cucumber-report.json        ← JSON format (for integrations)
├── cucumber-report.xml         ← XML format (for CI/CD)
└── screenshots/                ← Failure screenshots
    ├── Complete_full_funnel_FAILED.png
    ├── Form_validation_FAILED.png
    └── ... (if any tests fail)
```

### To View Reports
```bash
# Open Extent Report (recommended)
open /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/extent-report.html

# Or open Cucumber Report
open /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/cucumber-report.html
```

---

## ✅ Expected Test Results

### Scenario 1: Complete Full Funnel (@happy-path) ✅
```
✓ Given I navigate to the checkout page
✓ When I select a product on the main page
✓ And I fill in the shipping address with valid details
✓ And I select a shipping method
✓ And I fill in the payment details with test card
✓ And I accept the terms and conditions
✓ And I click the complete purchase button
✓ Then I should be taken to an upsell page or thank you page
✓ And I navigate through any upsell pages
✓ Then I should land on the thank you page

EXPECTED RESULT: PASSED ✅
```

### Scenario 2: Direct to Thank You (@direct-thankyou) ✅
```
✓ All steps complete
✓ Thank you page reached without upsells

EXPECTED RESULT: PASSED ✅
```

### Scenario 3: Form Validation (@form-validation) ✅
```
✓ Empty form submission
✓ Validation errors displayed

EXPECTED RESULT: PASSED ✅
```

### Scenario 4: Payment Validation (@payment-validation) ✅
```
✓ Invalid card submission
✓ Payment error displayed

EXPECTED RESULT: PASSED ✅
```

### Scenario 5: Order Confirmation (@regression) ✅
```
✓ Order details verified
✓ Order number confirmed
✓ Price confirmed
✓ Address confirmed
✓ Items confirmed

EXPECTED RESULT: PASSED ✅
```

---

## 📋 Console Output Indicators

When running tests, you'll see these indicators:

```
✅ PASSED - Scenario/step completed successfully
❌ FAILED - Scenario/step failed
📄 Page loaded or detected
📦 Order submitted or product added
🛒 Cart/shipping operation
✓ Verification successful
⚠ Warning message
→ Flow progress
ℹ Information
🔍 Detection/search operation
```

---

## 🧪 What the Tests Verify

### Primary Order Flow
- ✓ Product selection works
- ✓ Shipping form fills correctly
- ✓ Payment form fills correctly
- ✓ Terms acceptance works
- ✓ Submit button functions
- ✓ Order submission succeeds

### Post-Purchase Page Detection
- ✓ System detects thank you page correctly
- ✓ System detects upsell page correctly
- ✓ Correct page type identified

### Upsell Processing (if present)
- ✓ Product added to upsell
- ✓ Shipping selected for upsell
- ✓ Upsell accepted and continued
- ✓ Redirect happens correctly
- ✓ Loop continues until thank you

### Order Verification
- ✓ Order number extracted
- ✓ Order price extracted
- ✓ Shipping address verified
- ✓ Order items listed
- ✓ All details logged

### Error Handling
- ✓ Form validation errors caught
- ✓ Payment errors detected
- ✓ Page detection errors handled
- ✓ Element not found handled gracefully

---

## 🔧 Troubleshooting

### Issue: Maven command not found
```bash
# Install Maven
brew install maven

# Verify installation
mvn -version
```

### Issue: Java not found
```bash
# Install Java
brew install java

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home)
```

### Issue: Tests timeout
```bash
# Increase timeout in config.properties
timeout=30000  # Change from 10000 to 30000

# Or run with timeout parameter
mvn test -Dplaywright.timeout=30000
```

### Issue: Browser crashes
```bash
# Disable headless mode to see what's happening
# Edit config.properties:
headless=true  # Change to false

# Or add slow motion to see each step
slow.mo=500  # 500ms between actions
```

### Issue: Tests hang on upsell
```bash
# Check that upsell page elements are correct
# Review CheckoutStepDefs.java addUpsellProductToOrder() method
# Update selectors if needed
```

---

## 📊 Test Execution Workflow

```
1. Setup
   ├─ Load configuration
   ├─ Initialize Playwright
   ├─ Launch browser
   └─ Create new page

2. Execute Each Scenario
   ├─ Navigate to checkout
   ├─ Fill form & submit
   ├─ Detect page type
   ├─ Process upsells (if any)
   ├─ Verify thank you page
   └─ Capture order details

3. Teardown
   ├─ Close page
   ├─ Close browser context
   ├─ Close browser
   ├─ Generate reports
   └─ Attach screenshots (if failed)

4. Report Generation
   ├─ Create Extent Report HTML
   ├─ Create Cucumber Report HTML
   ├─ Create XML for CI/CD
   └─ Save screenshots
```

---

## 🎯 Success Criteria

A test run is successful when:

✅ All step definitions execute without errors  
✅ Page navigation happens correctly  
✅ Elements are found and interacted with  
✅ Validations pass  
✅ Order details are captured  
✅ Reports are generated  
✅ No unhandled exceptions occur  

---

## 📈 Sample Test Execution Timeline

```
13:45:00 - Starting Maven build
13:45:10 - Loading configuration
13:45:15 - Initializing Playwright
13:45:20 - Launching browser
13:45:30 - Starting Scenario 1: Complete Full Funnel
13:45:35 - Navigate to checkout page ✓
13:45:40 - Select product ✓
13:45:50 - Fill shipping address ✓
13:46:00 - Select shipping method ✓
13:46:10 - Fill payment details ✓
13:46:15 - Accept terms ✓
13:46:20 - Submit order ✓
13:46:30 - Detect: Upsell page ✓
13:46:35 - Add product to upsell ✓
13:46:40 - Select upsell shipping ✓
13:46:45 - Accept & continue upsell ✓
13:46:55 - Detect: Thank you page ✓
13:47:00 - Verify order details ✓
13:47:05 - Capture order number ✓
13:47:10 - Capture order price ✓
13:47:15 - Capture shipping address ✓
13:47:20 - Capture order items ✓
13:47:25 - Close browser ✓
13:47:30 - Generate reports ✓
13:47:35 - Test Complete: PASSED ✅

Total Time: ~2 minutes per scenario
```

---

## 🔗 Key File References

### Main Test Orchestration
- `src/test/java/com/funnel1pg/stepdefs/CheckoutStepDefs.java`

### Page Object Models
- `src/test/java/com/funnel1pg/pages/CheckoutPage.java`
- `src/test/java/com/funnel1pg/pages/UpsellPage.java`
- `src/test/java/com/funnel1pg/pages/ThankYouPage.java`

### Test Scenarios
- `src/test/resources/features/checkout_flow.feature`

### Test Data
- `src/test/resources/testdata/checkout_data.json`

### Configuration
- `src/test/resources/config.properties`

### Test Infrastructure
- `src/test/java/com/funnel1pg/hooks/Hooks.java`
- `src/test/java/com/funnel1pg/utils/PlaywrightManager.java`
- `src/test/java/com/funnel1pg/utils/ExtentReportManager.java`

---

## ✨ Tips for Running Tests

1. **Run in browser mode first** (headless=false) to watch what happens
2. **Check the console output** for detailed step information
3. **Review test reports** after execution for full details
4. **Take screenshots** on failures to debug issues
5. **Use slow motion** (slow.mo=500) if tests run too fast to debug
6. **Check logs** if anything fails unexpectedly
7. **Verify selectors** match your actual application elements

---

## 📞 Support

If tests don't pass:

1. Check console output for error messages
2. Open failure screenshots if available
3. Review QUICK_REFERENCE.md troubleshooting section
4. Check inline code comments in step definitions
5. Verify page elements match selectors in page objects
6. Check network connectivity to the test URL
7. Verify test data in checkout_data.json

---

**Test Framework**: Playwright + Cucumber BDD + JUnit  
**Status**: ✅ Ready to Run  
**Last Updated**: February 2025