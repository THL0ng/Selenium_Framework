package com.project.testcases;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.project.base.BasePage;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.$;

public class testCase_01 extends BasePage {
    @Test
    public void test() {
        Selenide.open("https://www.google.com");

        // Tạo element cần click
        SelenideElement myButton = $("div.FPdoLc > center > input.RNmpXc");
        // Gọi hàm click của BasePage (phải truyền đủ 2 tham số)
        click(myButton, "Click vào nút tìm kiếm Google");
    }
}
