package com.weba11y.server.repository.implement;

import com.weba11y.server.repository.AccessibilityViolationCustomRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccessibilityViolationCustomRepositoryImpl implements AccessibilityViolationCustomRepository {
    private final EntityManager em;
}
