package com.funnel1pg.diagnostic;

import com.funnel1pg.config.ConfigReader;
import com.funnel1pg.utils.PlaywrightManager;
import com.microsoft.playwright.Page;

/**
 * Diagnostic tool to inspect page elements
 * Helps identify actual selectors on the website
 */
public class PageDiagnostic {

    public static void main(String[] args) {
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         🔍 PAGE DIAGNOSTIC TOOL - STARTING                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        
        Page page = null;
        try {
            // Initialize browser
            PlaywrightManager.initBrowser();
            page = PlaywrightManager.getPage();
            
            // Navigate to checkout page
            String baseUrl = ConfigReader.getBaseUrl();
            System.out.println("\n📍 Navigating to: " + baseUrl);
            page.navigate(baseUrl);
            page.waitForLoadState();
            
            System.out.println("✅ Page loaded successfully\n");
            
            // Run diagnostics
            analyzeCheckoutPage(page);
            
        } catch (Exception e) {
            System.out.println("❌ FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (page != null) {
                PlaywrightManager.closeBrowser();
            }
            System.out.println("\n✅ Diagnostic complete\n");
        }
    }
    
    private static void analyzeCheckoutPage(Page page) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("📄 PAGE STRUCTURE ANALYSIS");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
        
        System.out.println("URL: " + page.url());
        System.out.println("Title: " + page.title());
        
        // Element counts
        System.out.println("\n📊 ELEMENT INVENTORY:");
        System.out.println("  • Buttons: " + page.locator("button").count());
        System.out.println("  • Links (a tags): " + page.locator("a").count());
        System.out.println("  • Headings: " + page.locator("h1, h2, h3, h4").count());
        System.out.println("  • Form inputs: " + page.locator("input").count());
        System.out.println("  • Divs: " + page.locator("div").count());
        System.out.println("  • Sections: " + page.locator("section").count());
        
        // All headings
        System.out.println("\n📝 ALL HEADINGS:");
        try {
            var headings = page.locator("h1, h2, h3, h4").all();
            for (int i = 0; i < headings.size(); i++) {
                String text = headings.get(i).textContent().trim();
                if (!text.isEmpty()) {
                    System.out.println("  [" + i + "] " + text);
                }
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        // All buttons with text
        System.out.println("\n🔘 ALL BUTTONS:");
        try {
            var buttons = page.locator("button").all();
            for (int i = 0; i < buttons.size(); i++) {
                String text = buttons.get(i).textContent().trim();
                if (!text.isEmpty()) {
                    System.out.println("  [" + i + "] " + text);
                }
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        // All links with text
        System.out.println("\n🔗 ALL LINKS:");
        try {
            var links = page.locator("a").all();
            int count = 0;
            for (var link : links) {
                String text = link.textContent().trim();
                String href = link.getAttribute("href");
                if (!text.isEmpty() && href != null && !href.isEmpty()) {
                    System.out.println("  [" + count + "] " + text + " → " + href);
                    count++;
                    if (count >= 15) {
                        System.out.println("  ... and " + (links.size() - 15) + " more");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        // Form fields
        System.out.println("\n📋 FORM FIELDS:");
        try {
            var inputs = page.locator("input").all();
            for (int i = 0; i < Math.min(20, inputs.size()); i++) {
                String type = inputs.get(i).getAttribute("type");
                String name = inputs.get(i).getAttribute("name");
                String id = inputs.get(i).getAttribute("id");
                String placeholder = inputs.get(i).getAttribute("placeholder");
                System.out.println("  [" + i + "] type=" + type + " name=" + name + " id=" + id + " placeholder=" + placeholder);
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        // Elements with product keyword
        System.out.println("\n🛍️ PRODUCT-RELATED ELEMENTS:");
        try {
            var products = page.locator("[class*='product'], [id*='product']").all();
            System.out.println("  Found " + products.size() + " elements with 'product' in class/id");
            for (int i = 0; i < Math.min(5, products.size()); i++) {
                String text = products.get(i).textContent().trim();
                if (!text.isEmpty() && text.length() < 100) {
                    System.out.println("    [" + i + "] " + text);
                }
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        
        // Try to find "Add" buttons or similar
        System.out.println("\n➕ ADD/SELECT BUTTONS:");
        try {
            var buttons = page.locator("button:has-text('Add'), button:has-text('Select'), button:has-text('Buy'), a:has-text('Add'), a:has-text('Select')").all();
            System.out.println("  Found " + buttons.size() + " add/select buttons");
            for (int i = 0; i < Math.min(5, buttons.size()); i++) {
                String text = buttons.get(i).textContent().trim();
                System.out.println("    [" + i + "] " + text);
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
}
