package com.weba11y.server.jpa.repository;

import com.weba11y.server.domain.InspectionUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionUrlRepository extends JpaRepository<InspectionUrl, Long>, InspectionUrlCustomRepository {

    List<InspectionUrl> findAllByMemberIdAndParentId(Long memberId, Long parentId);
}
