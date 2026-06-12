package com.project.base;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.util.Map;

public class BrowserManager {

    public static void setup() {
        // Cấu hình bắt buộc
        Configuration.baseUrl = ConfigLoader.getRequiredProperty("url");

        // Cấu hình tùy chọn
        Configuration.browser = ConfigLoader.getOptionalProperty("browser", "chrome");
        Configuration.headless = Boolean.parseBoolean(ConfigLoader.getOptionalProperty("headless", "false"));
        Configuration.timeout = Long.parseLong(ConfigLoader.getOptionalProperty("timeout", "10000"));

        // Áp dụng Options
        Configuration.browserCapabilities = getBrowserOptions(Configuration.browser.toLowerCase());
    }

    private static MutableCapabilities getBrowserOptions(String browser) {
        String startArg = Configuration.headless ? "--window-size=1920,1080" : "--start-maximized";

        if (browser.equals("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments(startArg, "--disable-notifications", "--disable-infobars");
            options.setExperimentalOption("prefs", Map.of(
                    "profile.default_content_setting_values.notifications", 2,
                    "profile.default_content_setting_values.geolocation", 2
            ));
            return options;
        } else if (browser.equals("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments(startArg);
            options.addPreference("dom.webnotifications.enabled", false);
            return options;
        }
        throw new IllegalArgumentException("Trình duyệt không được hỗ trợ: " + browser);
    }
}