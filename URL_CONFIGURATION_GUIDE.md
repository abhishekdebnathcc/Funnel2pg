# 🔧 URL Configuration & Environment Management

## Overview

The application URL is now fully externalized and can be easily changed based on requirements, environments, or deployment stages. This allows the same test suite to run against different environments without code changes.

---

## 📍 Current Configuration

**File**: `src/test/resources/config.properties`

**Current URL**:
```properties
base.url=https://stagingabhishek.gupigayen.com/1pgCC23Feb/
```

---

## 🚀 How to Change the URL

### Method 1: Edit config.properties (Default)

**File**: `src/test/resources/config.properties`

Simply update the `base.url` property:

```properties
# Original
base.url=https://stagingabhishek.gupigayen.com/1pgCC23Feb/

# Changed to production
base.url=https://production.gupigayen.com/1pgCC23Feb/
```

Then run tests normally:
```bash
mvn clean test
```

---

### Method 2: Command Line Override (Recommended for CI/CD)

Override the URL via Maven command line parameter:

```bash
# Run with production URL
mvn clean test -Dbase.url=https://production.gupigayen.com/1pgCC23Feb/

# Run with staging URL
mvn clean test -Dbase.url=https://staging2.gupigayen.com/1pgCC23Feb/

# Run with local development URL
mvn clean test -Dbase.url=http://localhost:3000/funnel/
```

**Advantages**:
- No file changes needed
- Easy for CI/CD pipelines
- Can change per test run
- No accidental commits of URLs

---

### Method 3: Multiple Environment Profiles

Create separate configuration files for each environment:

**File Structure**:
```
src/test/resources/
├── config.properties              (default)
├── config-staging.properties      (staging environment)
├── config-production.properties   (production environment)
└── config-local.properties        (local development)
```

**config-staging.properties**:
```properties
base.url=https://stagingabhishek.gupigayen.com/1pgCC23Feb/
browser=chromium
headless=false
```

**config-production.properties**:
```properties
base.url=https://production.gupigayen.com/1pgCC23Feb/
browser=chromium
headless=true
```

**config-local.properties**:
```properties
base.url=http://localhost:3000/funnel/
browser=chromium
headless=false
```

Then run with Maven profiles (requires pom.xml configuration):
```bash
# Run against staging
mvn clean test -Pstaging

# Run against production
mvn clean test -Pproduction

# Run against local
mvn clean test -Plocal
```

---

## 📝 Common URL Scenarios

### Development/Local
```bash
mvn clean test -Dbase.url=http://localhost:3000/funnel/
```

### Staging Environment
```bash
mvn clean test -Dbase.url=https://staging.gupigayen.com/1pgCC23Feb/
```

### Production Environment
```bash
mvn clean test -Dbase.url=https://production.gupigayen.com/1pgCC23Feb/
```

### Testing with Different Paths
```bash
mvn clean test -Dbase.url=https://staging.gupigayen.com/checkout/page1/
```

---

## 🔄 How URL is Used in Tests

The URL is loaded from configuration and used in the checkout navigation step:

**File**: `src/test/java/com/funnel1pg/stepdefs/CheckoutStepDefs.java`

```java
@Given("I navigate to the checkout page")
public void navigateToCheckout() {
    init();
    page.navigate(ConfigReader.getBaseUrl());  // ← Loads URL from config
    page.waitForLoadState();
    log("📄 Loaded: " + page.url());
}
```

**How it works**:
1. `ConfigReader.getBaseUrl()` reads the `base.url` property
2. Property is loaded from `config.properties` or command line override
3. URL is used to navigate the browser to the application

---

## ⚙️ Configuration Resolution Order

The system resolves URL in this order (first match wins):

1. **Command Line Parameter**: `-Dbase.url=https://...`
   - Highest priority
   - Overrides everything else
   - Used: `mvn test -Dbase.url=...`

2. **System Property**: Set in environment
   - Second priority
   - Example: `export MAVEN_OPTS="-Dbase.url=..."`

3. **config.properties File**: Committed to repository
   - Third priority
   - Default configuration
   - File: `src/test/resources/config.properties`

---

## 🔑 ConfigReader Implementation

**File**: `src/test/java/com/funnel1pg/config/ConfigReader.java`

```java
public class ConfigReader {
    
    public static String getBaseUrl() {
        // First check system property (command line override)
        String baseUrl = System.getProperty("base.url");
        
        // If not provided, load from config.properties
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = properties.getProperty("base.url");
        }
        
        return baseUrl.trim();
    }
}
```

This means:
- Command line `-Dbase.url=...` always takes precedence
- Falls back to `config.properties` if not specified
- Trimmed of whitespace for cleanliness

---

## 📋 Complete Usage Examples

### Example 1: Running against Default URL
```bash
# Uses base.url from config.properties
mvn clean test -Dcucumber.filter.tags="@happy-path"
```

### Example 2: Running against Production with Happy Path
```bash
# Uses production URL instead of config.properties
mvn clean test \
  -Dbase.url=https://production.gupigayen.com/1pgCC23Feb/ \
  -Dcucumber.filter.tags="@happy-path"
```

### Example 3: Running All Tests Against Staging
```bash
# All tests run against staging environment
mvn clean test -Dbase.url=https://staging2.gupigayen.com/1pgCC23Feb/
```

### Example 4: Running with Headless Mode (CI/CD)
```bash
# Production URL + headless mode + no screenshot
mvn clean test \
  -Dbase.url=https://production.gupigayen.com/1pgCC23Feb/ \
  -Dheadless=true \
  -Dscreenshot.on.failure=false
```

### Example 5: Running Validation Tests on Multiple Environments
```bash
# Staging
mvn clean test \
  -Dbase.url=https://staging.gupigayen.com/ \
  -Dcucumber.filter.tags="@validation"

# Production
mvn clean test \
  -Dbase.url=https://production.gupigayen.com/ \
  -Dcucumber.filter.tags="@validation"
```

---

## 🔐 CI/CD Integration

### GitHub Actions Example
```yaml
name: Test Suite

on: [push]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Run Tests on Staging
        run: |
          mvn clean test \
            -Dbase.url=${{ secrets.STAGING_URL }} \
            -Dheadless=true
      
      - name: Run Tests on Production
        run: |
          mvn clean test \
            -Dbase.url=${{ secrets.PRODUCTION_URL }} \
            -Dheadless=true
```

### Jenkins Pipeline Example
```groovy
pipeline {
    agent any
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['staging', 'production'], description: 'Test Environment')
    }
    
    stages {
        stage('Test') {
            steps {
                script {
                    def url = params.ENVIRONMENT == 'production' 
                        ? credentials('PROD_URL')
                        : credentials('STAGING_URL')
                    
                    sh "mvn clean test -Dbase.url=${url}"
                }
            }
        }
    }
}
```

---

## 📊 Environment Variables Configuration

You can also use environment variables:

```bash
# Set environment variable
export TEST_URL=https://staging.gupigayen.com/1pgCC23Feb/

# Use in Maven command
mvn clean test -Dbase.url=$TEST_URL
```

Or in a script:
```bash
#!/bin/bash

# Define URLs for different environments
STAGING_URL="https://staging.gupigayen.com/1pgCC23Feb/"
PRODUCTION_URL="https://production.gupigayen.com/1pgCC23Feb/"
LOCAL_URL="http://localhost:3000/funnel/"

# Run tests based on parameter
if [ "$1" == "prod" ]; then
    mvn clean test -Dbase.url=$PRODUCTION_URL
elif [ "$1" == "staging" ]; then
    mvn clean test -Dbase.url=$STAGING_URL
else
    mvn clean test -Dbase.url=$LOCAL_URL
fi
```

Usage:
```bash
./run-tests.sh prod       # Run against production
./run-tests.sh staging    # Run against staging
./run-tests.sh            # Run against local (default)
```

---

## ✅ Verification

To verify the URL being used, check the console output:

```
📄 Loaded: https://stagingabhishek.gupigayen.com/1pgCC23Feb/
```

Or add debug logging:

```java
@Given("I navigate to the checkout page")
public void navigateToCheckout() {
    String url = ConfigReader.getBaseUrl();
    System.out.println("Using URL: " + url);  // ← Shows which URL is being used
    page.navigate(url);
}
```

---

## 🔄 Best Practices

1. **Don't commit sensitive URLs**
   - Use environment variables in CI/CD
   - Store URLs in secrets management

2. **Use command line for CI/CD**
   - Prevents accidental commits
   - Clear audit trail
   - Easy environment switching

3. **Keep config.properties for defaults**
   - Should be development/staging
   - Used when no override provided

4. **Use meaningful environment names**
   - `local`, `dev`, `staging`, `production`
   - Clear distinction between environments

5. **Document URL changes**
   - Keep track of URL format requirements
   - Document any special paths or parameters

---

## 🛠️ Troubleshooting

### Issue: Wrong URL Being Used

**Check**:
1. Console output shows loaded URL
2. Verify command line parameter: `-Dbase.url=...`
3. Check config.properties file
4. Verify ConfigReader implementation

### Issue: URL Not Recognized

**Check**:
1. URL must be valid and complete
2. Must include protocol (http/https)
3. No trailing spaces (ConfigReader.trim() handles this)
4. URL must be accessible from test environment

### Issue: Tests Running Against Wrong Environment

**Solution**:
```bash
# Clear any cached values
mvn clean

# Run with explicit URL
mvn test -Dbase.url=https://your-correct-url.com/
```

---

## 📝 Summary

The URL is now fully externalized and configurable:

✅ Default URL in `config.properties`  
✅ Command line override with `-Dbase.url=...`  
✅ Environment variable support  
✅ CI/CD friendly  
✅ No code changes needed for URL updates  
✅ Same test suite for multiple environments  

**All requirements met**: URL is now part of test data configuration! 🎉

---

**Last Updated**: February 2025  
**Status**: ✅ URL Configuration Externalized