package com.weba11y.server.infrastructure.persistence;

import com.weba11y.server.domain.violation.AccessibilityViolation;

import java.util.List;

public interface AccessibilityViolationCustomRepository {

    void saveAll(List<AccessibilityViolation> violations);
}
