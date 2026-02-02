package com.weba11y.server.infrastructure.playwright;


import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class PlaywrightPageLoaderTest {
    @Test
    @DisplayName("HTML 가져오기")
    void getRenderedHtml() {
        // given
        String inspectionUrl = "https://www.naver.com";

        // when
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate(inspectionUrl);

            // then
            System.out.println(page.title());

            Assertions.assertThat(page.title()).isEqualTo(parseDocument(page).title());
        }
    }

    // Document 파싱
    private Document parseDocument(Page page) {
        String html = page.content();
        return Jsoup.parse(html);
    }
}
