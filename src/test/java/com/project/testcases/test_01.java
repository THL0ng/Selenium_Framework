package com.project.testcases;

import com.project.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import static com.codeborne.selenide.Selenide.*;

public class test_01 extends BaseTest {

    private static final Logger logger = LogManager.getLogger(test_01.class);

    @Test(description = "Kiểm tra mở trình duyệt và load trang chủ")
    @Description("Mở URL cấu hình và xác thực title trang web")
    public void testOpenBrowser() {
        openPage("/");
        verifyPageTitle();
    }

    @Step("Mở trang web tại đường dẫn: {path}")
    private void openPage(String path) {
        logger.info("Đang mở trang chủ: {}", path);
        open(path);
    }

    @Step("Xác thực tiêu đề trang")
    private void verifyPageTitle() {
        String pageTitle = title();
        logger.info("Title trang web là: {}", pageTitle);
    }
}