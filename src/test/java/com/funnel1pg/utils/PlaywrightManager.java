package com.funnel1pg.utils;

import com.funnel1pg.config.ConfigReader;
import com.microsoft.playwright.*;

public class PlaywrightManager {

    private static final ThreadLocal<Playwright>     playwrightTL = new ThreadLocal<>();
    private static final ThreadLocal<Browser>        browserTL    = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextTL    = new ThreadLocal<>();
    private static final ThreadLocal<Page>           pageTL       = new ThreadLocal<>();

    /**
     * Always launches a fresh browser instance for each scenario.
     * Never reuses an existing session.
     */
    public static void initBrowser() {
        Playwright playwright = Playwright.create();
        playwrightTL.set(playwright);

        BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                .setHeadless(ConfigReader.isHeadless())
                .setSlowMo(ConfigReader.getSlowMo());

        Browser browser;
        switch (ConfigReader.getBrowser().toLowerCase()) {
            case "firefox": browser = playwright.firefox().launch(opts); break;
            case "webkit":  browser = playwright.webkit().launch(opts);  break;
            default:        browser = playwright.chromium().launch(opts);
        }
        browserTL.set(browser);

        BrowserContext ctx = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900));
        contextTL.set(ctx);

        Page page = ctx.newPage();
        page.setDefaultTimeout(ConfigReader.getTimeout());
        pageTL.set(page);
    }

    public static Page           getPage()    { return pageTL.get(); }
    public static BrowserContext getContext() { return contextTL.get(); }

    /**
     * Fully closes the browser instance and all associated resources.
     * Call this only for scenarios that should NOT leave the browser open.
     */
    public static void closeBrowser() {
        if (pageTL.get()       != null) { try { pageTL.get().close();       } catch (Exception ignored) {} pageTL.remove(); }
        if (contextTL.get()    != null) { try { contextTL.get().close();    } catch (Exception ignored) {} contextTL.remove(); }
        if (browserTL.get()    != null) { try { browserTL.get().close();    } catch (Exception ignored) {} browserTL.remove(); }
        if (playwrightTL.get() != null) { try { playwrightTL.get().close(); } catch (Exception ignored) {} playwrightTL.remove(); }
    }
}
