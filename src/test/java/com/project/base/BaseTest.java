package com.project.base;

import com.codeborne.selenide.Selenide;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    @BeforeSuite
    public void setUp() {
        BrowserManager.setup();
    }

    @AfterMethod // Chạy sau mỗi test case
    public void tearDown() {
        Selenide.closeWebDriver();
    }
}