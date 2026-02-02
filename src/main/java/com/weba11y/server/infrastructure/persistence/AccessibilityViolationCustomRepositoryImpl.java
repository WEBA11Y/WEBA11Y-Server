package com.weba11y.server.infrastructure.persistence;

import com.weba11y.server.domain.violation.AccessibilityViolation;
import com.weba11y.server.infrastructure.persistence.AccessibilityViolationCustomRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccessibilityViolationCustomRepositoryImpl implements AccessibilityViolationCustomRepository {
    private final EntityManager em;
    private static final int BATCH_SIZE = 500;

    @Override
    public void saveAll(List<AccessibilityViolation> violations) {
        for(int i = 0; i < violations.size(); i++){
            em.persist(violations.get(i));
            if((i+1) % BATCH_SIZE == 0){
                em.flush();
                em.clear();
            }
        }
        em.flush();
    }
}
