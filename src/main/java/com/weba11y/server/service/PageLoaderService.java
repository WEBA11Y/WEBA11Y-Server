package com.weba11y.server.service;

import com.microsoft.playwright.Page;

public interface PageLoaderService {

    Page getLoadedPage(String inspectionUrl);
}
