package com.weba11y.server.repository;

import com.weba11y.server.domain.InspectionUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InspectionUrlRepository extends JpaRepository<InspectionUrl, Long>, InspectionUrlCustomRepository {
    List<InspectionUrl> findAllByMemberIdAndParentId(Long memberId, Long parentId);


    @Query("SELECT iu FROM InspectionUrl iu LEFT JOIN iu.inspectionSummaries is " +
            "WHERE iu.parent IS NULL " + // 제일 상위 Entity
            "AND iu.member.id = :memberId " +
            "AND iu.status != 'HIDE' " +
            "GROUP BY iu " +
            "ORDER BY COALESCE(MAX(is.createDate), iu.updateDate) DESC") // 검사 결과 날짜 최신순 또는 URL updateDate 최신순
    Page<InspectionUrl> findParentInspectionUrlsByMemberId(@Param("memberId") Long memberId, Pageable pageable);


    @Query("SELECT i.id FROM InspectionUrl i WHERE i.member.id = :memberId AND i.url = :url")
    Optional<Long> findIdByMemberIdAndUrl(@Param("memberId") Long memberId, @Param("url") String url);

    @Query("SELECT iu " +
            "FROM InspectionUrl iu LEFT JOIN iu.inspectionSummaries is " +
            "WHERE iu.member.id = :memberId " +
            "ORDER BY is.updateDate desc " +
            "LIMIT 1")
    Optional<InspectionUrl> findLatestInspectionUrlByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT i FROM InspectionUrl i LEFT JOIN FETCH i.child WHERE i.parent.id = :parentId")
    List<InspectionUrl> findAllByParentId(@Param("parentId") Long parentId);
}