# Funnel1pg — Playwright BDD Automation Framework

End-to-end smoke & validation test suite for the **Power Pro Heat** funnel checkout.

**Stack:** Java 11 · Maven · Playwright 1.41 · Cucumber 7.15 · JUnit 5 · ExtentReports 5

---

## Project Structure

```
src/test/
├── java/com/funnel1pg/
│   ├── config/
│   │   └── ConfigReader.java           ← config.properties loader
│   ├── hooks/
│   │   └── Hooks.java                  ← Before/After: browser + ExtentReports + screenshots
│   ├── pages/
│   │   ├── BasePage.java               ← shared Playwright helpers
│   │   ├── CheckoutPage.java           ← main checkout form (POM)
│   │   ├── UpsellPage.java             ← upsell navigation logic (POM)
│   │   └── ThankYouPage.java           ← order confirmation assertions (POM)
│   ├── runners/
│   │   ├── SmokeTestRunner.java        ← @smoke tests
│   │   ├── RegressionTestRunner.java   ← @smoke + @regression tests
│   │   └── ValidationTestRunner.java  ← @validation (negative) tests
│   ├── stepdefs/
│   │   └── CheckoutStepDefs.java       ← all Gherkin step implementations
│   └── utils/
│       ├── PlaywrightManager.java      ← ThreadLocal browser lifecycle
│       ├── ExtentReportManager.java    ← ExtentReports singleton + ThreadLocal test
│       ├── TestDataReader.java         ← JSON test data reader
│       ├── ScreenshotUtil.java         ← manual screenshot util
│       └── WaitUtils.java             ← reusable wait/check helpers
└── resources/
    ├── features/
    │   └── checkout_flow.feature       ← BDD scenarios (happy-path + validation)
    ├── testdata/
    │   └── checkout_data.json          ← customer + payment test data
    ├── config.properties               ← browser, URL, timeout settings
    └── junit-platform.properties       ← JUnit platform config
```

---

## Test Scenarios

| Tag | Scenario | Description |
|-----|----------|-------------|
| `@smoke @happy-path` | Full funnel order flow | Checkout → upsells → thank you page |
| `@validation @negative` | Empty form submission | Verifies required field validation errors |
| `@validation @negative` | Invalid card number | Verifies payment error message |

---

## Running Tests

```bash
# Smoke tests (default)
mvn test

# Regression (smoke + regression)
mvn test -Drunner=RegressionTestRunner

# Validation / negative tests
mvn test -Drunner=ValidationTestRunner

# Headless (CI/CD)
mvn test -Dheadless=true

# Different browser
mvn test -Dbrowser=firefox
mvn test -Dbrowser=webkit

# Slow motion for debugging
mvn test -Dslow.mo=500
```

---

## Reports

After execution:

| Report | Location |
|--------|----------|
| ExtentReports HTML | `reports/extent-report.html` |
| Cucumber HTML | `reports/cucumber-report.html` |
| Cucumber JSON | `reports/cucumber-report.json` |
| JUnit XML | `reports/cucumber-report.xml` |
| Failure screenshots | `reports/screenshots/` |

---

## First-Time Setup

```bash
# Install Playwright browsers
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

# Then run
mvn test
```

---

## Configuration

`src/test/resources/config.properties`:

```properties
base.url=https://stagingabhishek.gupigayen.com/1pgCC23Feb/
browser=chromium          # chromium | firefox | webkit
headless=false            # true for CI/CD
slow.mo=0                 # ms delay between actions (useful for debugging)
timeout=30000             # default element timeout in ms
screenshot.on.failure=true
reports.dir=reports
```

---

## Git

```bash
git remote add origin <your-repo-url>
git push -u origin main
```
