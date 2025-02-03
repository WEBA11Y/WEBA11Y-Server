package com.weba11y.server.repository.implement;

import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.repository.InspectionUrlCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InspectionUrlCustomRepositoryImpl implements InspectionUrlCustomRepository {

    private final EntityManager em;

    @Override
    public List<InspectionUrl> findAllByMemberId(Long memberId) {
        return em.createQuery("SELECT DISTINCT iu FROM InspectionUrl iu " +
                                "LEFT JOIN FETCH iu.parent " +
                                "LEFT JOIN FETCH iu.child c " +
                                "LEFT JOIN FETCH c.child " +
                                "WHERE iu.parent IS NULL AND iu.member.id = :memberId",
                        InspectionUrl.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }


    @Override
    public Optional<InspectionUrl> findByIdAndMemberId(Long urlId, Long memberId) {
        try {
            InspectionUrl inspectionUrl = em.createQuery(
                            "SELECT DISTINCT iu FROM InspectionUrl iu " +
                                    "LEFT JOIN FETCH iu.child c " +  // 자식 URL을 함께 가져옴
                                    "LEFT JOIN FETCH c.child " +      // 자식의 자식 URL도 함께 가져옴
                                    "WHERE iu.id = :urlId AND iu.member.id = :memberId",
                            InspectionUrl.class)
                    .setParameter("urlId", urlId)
                    .setParameter("memberId", memberId)
                    .getSingleResult();

            return Optional.of(inspectionUrl);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
