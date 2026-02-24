# 🌐 Auto-Open Report in Chrome

## Feature Overview

After test execution completes, the Extent Report automatically opens in Google Chrome browser. No manual action needed!

---

## How It Works

1. Tests execute normally
2. All scenarios complete
3. Reports are generated (Extent + Cucumber)
4. Chrome browser automatically opens
5. Extent Report displays in Chrome tab

---

## Supported Platforms

✅ **macOS** - Uses `open -a "Google Chrome"`  
✅ **Windows** - Uses `start chrome`  
✅ **Linux** - Uses `google-chrome`  

---

## Implementation

### Auto-Open Mechanism

**File**: `src/test/java/com/funnel1pg/utils/ReportOpener.java`

The `ReportOpener` utility:
- Detects the operating system
- Locates the Extent Report file
- Opens it in Chrome using OS-specific commands
- Includes fallback mechanisms if Chrome is not available

### Integration Point

**File**: `src/test/java/com/funnel1pg/hooks/Hooks.java`

The `@AfterAll` hook calls:
```java
ReportOpener.openReportInChrome();
```

This runs after all test scenarios complete and reports are generated.

---

## How to Use

No setup needed! Just run tests normally:

```bash
mvn clean test
```

After tests complete, Chrome will automatically open with the report.

---

## Report Location

The opened report is located at:
```
reports/extent-report.html
```

The file URL in Chrome:
```
file:///Users/codeclouds-abhishek/IdeaProjects/Funnel1pg/reports/extent-report.html
```

---

## OS-Specific Details

### macOS

**Command Used**:
```bash
open -a "Google Chrome" file:///path/to/report.html
```

**Fallback**:
If Chrome not found, uses generic `open` command with default browser

**Requirements**:
- Chrome must be installed
- Chrome application visible to system

### Windows

**Command Used**:
```cmd
start chrome "file:///path/to/report.html"
```

**Fallback**:
If Chrome not found, uses `start` command with default browser

**Requirements**:
- Chrome must be installed and in PATH
- Alternatively: Chrome shortcut in Start Menu

### Linux

**Command Used**:
```bash
google-chrome file:///path/to/report.html
```

**Fallback**:
If Chrome not found, uses `xdg-open` with default browser

**Requirements**:
- Chrome must be installed
- Accessible via `google-chrome` command

---

## Alternative: Desktop API Method

If the OS-specific method fails, there's a fallback using Java Desktop API:

```java
ReportOpener.openReportUsingDesktopAPI();
```

This method:
- Works across all platforms automatically
- Opens in system's default browser
- Requires Java Desktop support

---

## Troubleshooting

### Chrome Not Opening

**Issue**: Chrome doesn't open after tests complete

**Solutions**:

1. **Verify Chrome is installed**:
   ```bash
   # macOS
   open -a "Google Chrome" --version
   
   # Windows
   chrome --version
   
   # Linux
   google-chrome --version
   ```

2. **Check report file exists**:
   ```bash
   ls -la reports/extent-report.html
   ```

3. **Manually open report**:
   ```bash
   # macOS
   open reports/extent-report.html
   
   # Windows
   start reports/extent-report.html
   
   # Linux
   xdg-open reports/extent-report.html
   ```

4. **Check console output**:
   Look for error messages in test output like:
   ```
   ❌ Error opening report: ...
   ```

### Chrome Already Running

Chrome doesn't need to close and reopen. The report opens in a new tab in the existing Chrome window.

---

## Console Output

When report opens successfully:

```
📊 Extent report saved → reports/extent-report.html

🌐 Opening test report in Chrome...
✅ Report opened in Chrome
```

If there's an issue:

```
⚠️ Chrome not found, trying generic open...
```

or

```
❌ Error opening report: ...
📄 Please open manually: /path/to/report.html
```

---

## Customization

### To Use Different Browser

You can modify `ReportOpener.java` to use a different browser:

**Firefox Example**:
```java
ProcessBuilder pb = new ProcessBuilder(
    "open",           // or "start" on Windows
    "-a",
    "Firefox",        // Change from Chrome to Firefox
    fileUrl
);
```

**Safari Example**:
```java
ProcessBuilder pb = new ProcessBuilder(
    "open",
    "-a",
    "Safari",
    fileUrl
);
```

### To Disable Auto-Open

Comment out the call in `Hooks.java`:

```java
@AfterAll
public static void afterAll() {
    ExtentReportManager.flushReports();
    System.out.println("📊 Extent report saved → reports/extent-report.html");
    
    // Disable auto-open by commenting:
    // ReportOpener.openReportInChrome();
}
```

### To Add Delay Before Opening

Add a delay in `Hooks.java`:

```java
@AfterAll
public static void afterAll() {
    ExtentReportManager.flushReports();
    System.out.println("📊 Extent report saved → reports/extent-report.html");
    
    try {
        // Wait 2 seconds before opening
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        // Ignore
    }
    
    ReportOpener.openReportInChrome();
}
```

---

## Key Features

✅ **Automatic** - No manual clicking needed  
✅ **Cross-Platform** - Works on macOS, Windows, Linux  
✅ **Smart Fallbacks** - Multiple methods to open report  
✅ **Error Handling** - Graceful handling if Chrome not available  
✅ **File URL** - Uses proper file:// protocol  
✅ **Configurable** - Easy to customize browser choice  

---

## Files Modified

1. **Hooks.java** - Added ReportOpener call in @AfterAll
2. **ReportOpener.java** - New utility class (created)

---

## Testing the Feature

### To verify report auto-opens:

```bash
mvn clean test -Dcucumber.filter.tags="@happy-path"
```

**Expected Result**:
1. Test executes
2. "Report saved" message appears
3. Chrome opens automatically
4. Extent Report displays

### To test with all scenarios:

```bash
mvn clean test
```

---

**Status**: ✅ Feature Implemented  
**Works On**: macOS, Windows, Linux  
**Report Format**: HTML (Extent Report)  
**Browser**: Google Chrome (default)  

---

**Last Updated**: February 2025