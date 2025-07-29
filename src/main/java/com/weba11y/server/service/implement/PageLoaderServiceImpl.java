package com.weba11y.server.service.implement;

import com.microsoft.playwright.*;
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

    /**
     * URL을 로드하고 SPA/동적 페이지에서도 DOM이 안정화될 때까지 대기
     */
    public Page getLoadedPage(String url) {
        Page page = null;
        int retries = 3;

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                page = browser.newPage();
                log.info("[PlaywrightService] Navigating to URL (attempt {}): {}", attempt, url);

                // 1) 페이지 이동
                page.navigate(url, new Page.NavigateOptions()
                        .setTimeout(Duration.ofSeconds(60).toMillis()));

                // 2) DOMContentLoaded 대기
                page.waitForLoadState(LoadState.DOMCONTENTLOADED,
                        new Page.WaitForLoadStateOptions().setTimeout(Duration.ofSeconds(30).toMillis()));

                // 3) 네트워크 활동 안정화
                page.waitForLoadState(LoadState.NETWORKIDLE,
                        new Page.WaitForLoadStateOptions().setTimeout(Duration.ofSeconds(60).toMillis()));

                // 4) 주요 DOM 요소(body) 로드 확인
                page.waitForSelector("body", new Page.WaitForSelectorOptions().setTimeout(30000));

                // 5) SPA(Hydration) 완료 대기
                waitForHydration(page);

                log.info("[PlaywrightService] Page fully loaded and stable: {}", url);
                return page;

            } catch (Exception e) {
                log.warn("[PlaywrightService] Attempt {} failed for URL {}: {}", attempt, url, e.getMessage());

                if (page != null) {
                    try { page.close(); } catch (Exception ignore) {}
                }

                if (attempt == retries) {
                    log.error("[PlaywrightService] All attempts failed for URL: {}", url, e);
                    throw new RuntimeException("Error loading page: " + e.getMessage(), e);
                }

                try {
                    Thread.sleep(2000L); // 재시도 전에 잠시 대기
                } catch (InterruptedException ignored) {}
            }
        }
        throw new IllegalStateException("Unexpected error during page load");
    }

    /**
     * SPA 환경에서 Hydration(React/Vue 렌더링) 완료 대기
     * DOM에 주요 인터랙션 요소가 나타날 때까지 반복 체크
     */
    private void waitForHydration(Page page) {
        try {
            page.waitForSelector("[data-hydrated], [id], [class]", new Page.WaitForSelectorOptions()
                    .setTimeout(5000));
            log.debug("[PlaywrightService] SPA hydration complete");
        } catch (PlaywrightException e) {
            log.warn("[PlaywrightService] Hydration check timeout (continuing anyway)");
        }
    }

    /**
     * ElementHandle로부터 outerHTML을 안전하게 추출
     * (DOM이 교체되거나 GC 된 경우 예외 방지)
     */
    public String safeOuterHtml(ElementHandle element) {
        try {
            Object html = element.evaluate("el => el.outerHTML");
            return html != null ? html.toString() : "<unavailable>";
        } catch (Exception e) {
            log.warn("[PlaywrightService] Failed to extract outerHTML: {}", e.getMessage());
            return "<unavailable>";
        } finally {
            try {
                element.dispose(); // ElementHandle 리소스 해제 (메모리 누수 방지)
            } catch (Exception ignored) {}
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
