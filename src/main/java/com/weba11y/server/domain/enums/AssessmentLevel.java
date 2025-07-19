package com.weba11y.server.domain.enums;

public enum AssessmentLevel {
    A(1),
    AA(2),
    AAA(3),
    OUTSTANDING(4);

    private final int order;

    AssessmentLevel(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
