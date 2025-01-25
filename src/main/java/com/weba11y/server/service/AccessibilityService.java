package com.weba11y.server.service;

import java.util.List;

public interface AccessibilityService {

    List<String> checkAccessibility(String url);
}
