package com.weba11y.server.repository;

import com.weba11y.server.domain.InspectionUrl;

import java.util.List;
import java.util.Optional;

public interface InspectionUrlCustomRepository {
    List<InspectionUrl> findAllByMemberId(Long memberId);

    Optional<InspectionUrl> findByIdAndMemberId(Long urlId, Long memberId);

    Optional<InspectionUrl> findByUrlId(Long urlId);
    boolean existsByUrlAndMemberId(String url, Long memberId);
}
