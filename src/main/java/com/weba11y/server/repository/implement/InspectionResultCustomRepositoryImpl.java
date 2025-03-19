package com.weba11y.server.repository.implement;

import com.weba11y.server.repository.InspectionResultCustomRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InspectionResultCustomRepositoryImpl implements InspectionResultCustomRepository {
    private final EntityManager em;
}
