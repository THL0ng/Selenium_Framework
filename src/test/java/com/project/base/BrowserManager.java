package com.project.base;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BrowserManager {

    private static final List<String> SUPPORTED_BROWSERS = Arrays.asList("chrome", "firefox");

    public static void setup() {
        String browser   = resolve("browser", "chrome");
        boolean headless = Boolean.parseBoolean(resolve("headless", "false"));
        long timeout     = Long.parseLong(resolve("timeout", "10000"));
        String remoteUrl = resolveOptional("remote.url");

        validateBrowser(browser);

        Configuration.baseUrl             = ConfigLoader.getRequiredProperty("url");
        Configuration.browser             = browser;
        Configuration.headless            = headless;
        Configuration.timeout             = timeout;
        Configuration.browserCapabilities = buildOptions(browser, headless);

        if (remoteUrl != null && !remoteUrl.isBlank()) {
            Configuration.remote = remoteUrl;
        }
    }

    private static String resolve(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();
        return ConfigLoader.getOptionalProperty(key, defaultValue);
    }

    private static String resolveOptional(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();
        String fileProp = ConfigLoader.getOptionalProperty(key, null);
        return (fileProp != null && !fileProp.isBlank()) ? fileProp : null;
    }

    private static void validateBrowser(String browser) {
        if (!SUPPORTED_BROWSERS.contains(browser.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Browser không hỗ trợ: '" + browser + "'. Giá trị hợp lệ: " + SUPPORTED_BROWSERS
            );
        }
    }

    private static MutableCapabilities buildOptions(String browser, boolean headless) {
        String windowArg = headless ? "--window-size=1920,1080" : "--start-maximized";

        switch (browser.toLowerCase()) {
            case "chrome": {
                ChromeOptions options = new ChromeOptions();
                options.addArguments(
                        windowArg,
                        "--disable-notifications",
                        "--disable-infobars",
                        "--disable-dev-shm-usage",
                        "--no-sandbox"
                );
                options.setExperimentalOption("prefs", Map.of(
                        "profile.default_content_setting_values.notifications", 2,
                        "profile.default_content_setting_values.geolocation",   2
                ));
                return options;
            }
            case "firefox": {
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments(windowArg);
                options.addPreference("dom.webnotifications.enabled", false);
                options.addPreference("geo.enabled", false);
                return options;
            }
            default:
                throw new IllegalStateException("Unreachable: " + browser);
        }
    }
}