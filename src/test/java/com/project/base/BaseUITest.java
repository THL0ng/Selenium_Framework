package com.project.base;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

public abstract class BaseUITest {

    @BeforeSuite(alwaysRun = true)
    public void globalSetUp() {
        ConfigLoader.init();
    }

    @BeforeClass(alwaysRun = true)
    public void uiSetUp() {
        BrowserManager.setup();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfterTest() {
        if (WebDriverRunner.hasWebDriverStarted()) {
            Selenide.clearBrowserCookies();
            Selenide.clearBrowserLocalStorage();
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownBrowser() {
        if (WebDriverRunner.hasWebDriverStarted()) {
            Selenide.closeWebDriver();
        }
    }
}