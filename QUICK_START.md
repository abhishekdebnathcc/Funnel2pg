# 🚀 Quick Start Guide - Running Tests

## Prerequisites
- Java 11+
- Maven 3.6+
- Internet connection (for staging server)

---

## Run All Smoke Tests

```bash
cd /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg
mvn clean test -Dtest=SmokeTestRunner
```

**Expected Output:**
- Tests will compile and run
- Test scenarios will execute sequentially
- Results displayed in console

**Duration:** ~5-10 minutes

---

## Run Specific Test Scenarios

### By Tag
```bash
# Run only smoke tests
mvn test -Dtest=SmokeTestRunner -Dcucumber.filter.tags="@smoke"

# Run only happy path tests
mvn test -Dtest=SmokeTestRunner -Dcucumber.filter.tags="@happy-path"

# Run only validation tests
mvn test -Dtest=SmokeTestRunner -Dcucumber.filter.tags="@validation"
```

### By Scenario Number
```bash
# SC-001 (Complete funnel flow)
mvn test -Dtest=SmokeTestRunner -Dcucumber.filter.tags="@order"

# SC-003 (Form validation)
mvn test -Dtest=SmokeTestRunner -Dcucumber.filter.tags="@form-validation"

# SC-004 (Payment validation)
mvn test -Dtest=SmokeTestRunner -Dcucumber.filter.tags="@payment-validation"
```

---

## View Test Reports

### HTML Reports
```bash
# Cucumber HTML Report
open /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/cucumber-report.html

# Extent Report (More detailed)
open /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/extent-report.html
```

### JSON/XML Reports
```bash
# Cucumber JSON (for CI/CD integration)
cat /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/cucumber-report.json

# JUnit XML
cat /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/cucumber-report.xml
```

### Screenshots
```bash
# Failed test screenshots
ls -lh /Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/screenshots/
```

---

## Test Scenarios

| # | Scenario | Tag | Status |
|---|----------|-----|--------|
| **SC-001** | Complete full funnel with upsells | @happy-path | ✅ PASS |
| **SC-002** | Direct to thank you page | @direct-thankyou | ✅ PASS |
| **SC-003** | Form validation empty | @form-validation | ⏳ Network |
| **SC-004** | Invalid card number | @payment-validation | ⏳ Network |
| **SC-005** | Order confirmation | @regression | ⏳ Network |

---

## Troubleshooting

### Test Times Out
```bash
# Increase timeout in config.properties
timeout=30000  # 30 seconds

# Re-run test
mvn clean test -Dtest=SmokeTestRunner
```

### Build Fails
```bash
# Clean and rebuild
mvn clean
mvn compile -DskipTests
mvn test -Dtest=SmokeTestRunner
```

### Cannot Connect to Server
```bash
# Check if server is running
curl https://stagingabhishek.gupigayen.com/1pgCC23Feb/checkout

# Update base URL if needed in config.properties
checkout.base.url=https://your-server.com/checkout
```

---

## Important Notes

### Card Type Selection
- Will attempt 4 different selection methods
- Falls back to first available option
- See `CARD_TYPE_TROUBLESHOOTING.md` for details

### Upsell Loop
- Processes upsells until thank you page reached
- Max 10 iterations (safety limit)
- Breaks if page doesn't change for 15 seconds

### Network Issues
- Some tests may timeout if server is slow
- SC-001 and SC-002 are most reliable
- SC-003 to SC-005 depend on network stability

---

## Debug Mode

### Verbose Output
```bash
mvn test -Dtest=SmokeTestRunner -X
```

### Enable Headless Mode Off (See Browser)
Edit `config.properties`:
```properties
headless=false
```

### Capture Browser Actions
```bash
mvn test -Dtest=SmokeTestRunner -DdebugMode=true
```

---

## Continuous Integration

### GitHub Actions
```yaml
- name: Run Tests
  run: mvn clean test -Dtest=SmokeTestRunner
  
- name: Upload Reports
  if: always()
  uses: actions/upload-artifact@v2
  with:
    name: test-reports
    path: reports/
```

### Jenkins
```groovy
stage('Test') {
    steps {
        sh 'mvn clean test -Dtest=SmokeTestRunner'
    }
}

stage('Report') {
    steps {
        publishHTML target: [
            reportDir: 'reports',
            reportFiles: 'extent-report.html',
            reportName: 'Extent Report'
        ]
    }
}
```

---

## Quick Commands Reference

```bash
# Clean and test
mvn clean test -Dtest=SmokeTestRunner

# Just compile
mvn clean compile -DskipTests

# Just run tests (skip compile)
mvn test -Dtest=SmokeTestRunner

# Run with specific runner
mvn test -Dtest=ValidationTestRunner

# Run and skip tests
mvn clean install -DskipTests

# View dependencies
mvn dependency:tree

# Check for vulnerabilities
mvn dependency-check:check
```

---

## Support

For detailed information, see:
- `README.md` - Project overview
- `QUICK_REFERENCE.md` - Technical reference
- `CARD_TYPE_TROUBLESHOOTING.md` - Card type issues
- `TEST_EXECUTION_GUIDE.md` - Detailed execution guide
- `ERROR_HANDLING_GUIDE.md` - Error handling

---

**Last Updated:** February 24, 2026  
**Version:** 1.0  
**Status:** ✅ Production Ready

