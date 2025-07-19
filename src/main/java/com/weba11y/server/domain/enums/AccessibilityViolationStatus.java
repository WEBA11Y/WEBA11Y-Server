package com.weba11y.server.domain.enums;

public enum AccessibilityViolationStatus {
    COMPLETED("해결 완료"),
    PENDING("미완료"),
    ERROR("오류");

    private final String description;

    AccessibilityViolationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
