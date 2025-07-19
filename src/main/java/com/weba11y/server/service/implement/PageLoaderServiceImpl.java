package com.weba11y.server.service.implement;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.weba11y.server.service.PageLoaderService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class PageLoaderServiceImpl implements PageLoaderService {

    private Playwright playwright;
    private Browser browser;

    // Playwright, Browser 초기화
    @PostConstruct
    public void initialize() {
        log.info("Initializing Playwright and Browser instances...");
        try {
            this.playwright = Playwright.create();

            this.browser = this.playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setChannel("chromium")
                            .setArgs(java.util.Arrays.asList(
                                    "--disable-gpu",
                                    "--no-sandbox",
                                    "--disable-dev-shm-usage"
                            ))
            );
            log.info("Playwright and Browser instances initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize Playwright or Browser: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Playwright browser.", e);
        }
    }

    @Override
    public Page getLoadedPage(String inspectionUrl) {
        Page page = null;
        try {
            page = browser.newPage();
            log.info("Navigating to URL: {}", inspectionUrl);

            // 페이지 로드 및 대기 시간 설정
            page.navigate(inspectionUrl, new Page.NavigateOptions()
                    .setTimeout(Duration.ofSeconds(60).toMillis()));

            // 모든 네트워크 활동이 중단될 때까지 최대 45초 대기
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(Duration.ofSeconds(45).toMillis()));

            return page;
        } catch (Exception e) {
            log.error("Error loading page for URL: {}. Error: {}", inspectionUrl, e.getMessage(), e);
            if (page != null)
                page.close();
            throw new RuntimeException("Error loading page: " + e.getMessage(), e);
        }
    }

    // 스프링 빈이 소멸되기 전에 Browser, Playwright close
    @PreDestroy
    public void cleanup() {
        log.info("Closing Playwright and Browser instances...");
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        log.info("Playwright and Browser instances closed.");
    }
}
