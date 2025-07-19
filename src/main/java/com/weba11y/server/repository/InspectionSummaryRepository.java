package com.weba11y.server.repository;

import com.weba11y.server.domain.InspectionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InspectionSummaryRepository extends JpaRepository<InspectionSummary, Long> {

    @Query("SELECT is " +
            "FROM InspectionSummary is " +
            "WHERE is.inspectionUrl.id = :urlId " +
            "AND is.inspectionUrl.member.id = :memberId " +
            "ORDER BY is.createDate DESC " + // createDate 기준으로 내림차순 정렬 (가장 최신이 먼저 오도록)
            "LIMIT 1")
        // 결과 1개만 가져오도록 제한
    Optional<InspectionSummary> findLatestByUrlIdAndMemberId(
            @Param("urlId") Long urlId,
            @Param("memberId") Long memberId
    );


    @Query("SELECT is " +
            "FROM InspectionSummary is " +
            "WHERE is.inspectionUrl.id = :urlId " +
            "AND is.inspectionUrl.member.id = :memberId " +
            "ORDER BY is.updateDate DESC ")
    List<InspectionSummary> findAllByUrlIdAndMemberId(
            @Param("urlId") Long urlId,
            @Param("memberId") Long memberId
    );
}
