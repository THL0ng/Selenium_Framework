package com.project.base;

import org.testng.annotations.BeforeSuite;

public abstract class BaseAPITest {

    @BeforeSuite(alwaysRun = true)
    public void globalSetUp() {
        ConfigLoader.init();
    }
}