package com.weba11y.server.jpa.repository.implement;

import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.jpa.repository.InspectionUrlCustomRepository;
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
    public Optional<InspectionUrl> findByUrlId(Long urlId) {
        try {
            InspectionUrl inspectionUrl = em.createQuery("SELECT DISTINCT iu FROM InspectionUrl iu " +
                                    "LEFT JOIN FETCH iu.parent " +
                                    "LEFT JOIN FETCH iu.child c " +
                                    "LEFT JOIN FETCH c.child " +
                                    "WHERE iu.id =:urlId ",
                            InspectionUrl.class)
                    .setParameter("urlId", urlId)
                    .getSingleResult();
            return Optional.of(inspectionUrl);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

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
                                    "WHERE iu.id = :urlId " +
                                    "AND iu.member.id = :memberId " +
                                    "AND iu.status != 'HIDE' " + // iu.status가 HIDE가 아닌 경우
                                    "AND (c IS NULL OR c.status != 'HIDE')", // c가 NULL이거나 c.status가 HIDE가 아닌 경우
                            InspectionUrl.class)
                    .setParameter("urlId", urlId)
                    .setParameter("memberId", memberId)
                    .getSingleResult();

            return Optional.of(inspectionUrl);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<InspectionUrl> findParentByMemberId(Long memberId) {
        return em.createQuery("SELECT iu FROM InspectionUrl iu " +
                        "WHERE iu.member.id = :memberId " +
                        "AND iu.parent IS NULL", InspectionUrl.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    @Override
    public boolean existsByUrlAndMemberId(String url, Long memberId) {
        Long count = em.createQuery("SELECT COUNT(iu) FROM InspectionUrl iu " +
                        "WHERE iu.member.id = :memberId " +
                        "AND iu.url = :url", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("url", url)
                .getSingleResult();
        return count > 0;
    }

}
