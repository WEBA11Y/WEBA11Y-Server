package com.weba11y.server.infrastructure.playwright;

import com.microsoft.playwright.Page;

public interface PageLoaderService {

    Page getLoadedPage(String inspectionUrl);
}
