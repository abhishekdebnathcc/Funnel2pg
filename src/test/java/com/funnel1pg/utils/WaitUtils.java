package com.funnel1pg.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class WaitUtils {

    private WaitUtils() {}

    /**
     * Wait for an element to be visible with a custom timeout (ms).
     */
    public static void waitForVisible(Page page, String selector, int timeoutMs) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
    }

    /**
     * Wait for URL to contain a given substring.
     */
    public static void waitForUrlContains(Page page, String substring, int timeoutMs) {
        page.waitForURL("**" + substring + "**",
                new Page.WaitForURLOptions().setTimeout(timeoutMs));
    }

    /**
     * Wait for URL to change away from the given URL.
     */
    public static void waitForUrlChange(Page page, String currentUrl, int timeoutMs) {
        page.waitForURL(url -> !url.equals(currentUrl),
                new Page.WaitForURLOptions().setTimeout(timeoutMs));
    }

    /**
     * Wait for network to be idle (all requests settled).
     */
    public static void waitForNetworkIdle(Page page) {
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    /**
     * Retry clicking an element up to maxAttempts times.
     */
    public static void retryClick(Page page, String selector, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            try {
                page.locator(selector).first().click();
                return;
            } catch (Exception e) {
                if (i == maxAttempts - 1) throw e;
                page.waitForTimeout(500);
            }
        }
    }

    /**
     * Check if an element is present in the DOM (may not be visible).
     */
    public static boolean isPresent(Page page, String selector) {
        try {
            return page.locator(selector).count() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an element is visible on the page.
     */
    public static boolean isVisible(Page page, String selector) {
        try {
            return page.locator(selector).first().isVisible();
        } catch (Exception e) {
            return false;
        }
    }
}
