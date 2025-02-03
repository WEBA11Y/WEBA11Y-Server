package com.weba11y.server.repository;

import com.weba11y.server.domain.InspectionUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionUrlRepository extends JpaRepository<InspectionUrl, Long> {
        List<InspectionUrl> findAllByMemberId(Long memberId);

        List<InspectionUrl> findAllByMemberIdAndParentId(Long memberId, Long parentId);

        Optional<InspectionUrl> findByIdAndMemberId(Long urlId, Long memberId);
}
