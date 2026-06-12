package com.project.base;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.codeborne.selenide.Selenide.$;

public class BasePage {
    private static final Logger log = LoggerFactory.getLogger(BasePage.class);
    private static final int RETRY_DELAY_MS = 500; // Đưa ra làm hằng số



    @Step("Click: {description}")
    public void click(SelenideElement element, String description) {
        int maxRetries = 1;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                log.info("Click: [{}] (Lần thử {})", description, i + 1);

                // 1. Chờ cho các hiệu ứng loading biến mất trước khi thao tác
                waitLoading();

                // 2. Chờ element sẵn sàng và thực hiện click
                element.shouldBe(Condition.visible, Duration.ofMillis(Configuration.timeout))
                        .shouldBe(Condition.enabled)
                        .click();

                log.info("Click thành công: [{}]", description);
                return;

            } catch (Exception e) {
                if (i == maxRetries) {
                    log.error("Click thất bại sau {} lần thử: [{}]. Lỗi: {}", maxRetries + 1, description, e.getMessage());
                    throw e;
                }
                log.warn("Click lỗi, đợi 500ms để retry [{}]...", description);
                Selenide.sleep(RETRY_DELAY_MS);            }
        }
    }

    /**
     * Phương thức chờ loading chung cho toàn bộ Framework.
     * Tùy chỉnh selector '.loading-spinner' cho phù hợp với dự án của bạn.
     */
    private void waitLoading() {
        // Nếu trang web có spinner, đợi nó biến mất (đợi tối đa 5s)
        $(".loading-spinner").shouldNotBe(Condition.visible, Duration.ofSeconds(5));
    }
}
