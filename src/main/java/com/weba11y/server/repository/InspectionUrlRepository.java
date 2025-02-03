package com.weba11y.server.repository;

import com.weba11y.server.domain.InspectionUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionUrlRepository extends JpaRepository<InspectionUrl, Long>, InspectionUrlCustomRepository {

    List<InspectionUrl> findAllByMemberIdAndParentId(Long memberId, Long parentId);
}
