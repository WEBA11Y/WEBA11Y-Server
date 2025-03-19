package com.weba11y.server.service;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlDto;

import java.util.List;

public interface InspectionUrlService {

    InspectionUrlDto saveUrl(InspectionUrlDto.Request request, Member member);

    List<InspectionUrlDto.Response> retrieveAll(Long memberId);

    List<InspectionUrlDto.ParentOnlyResponse> retrieveParentUrl(Long memberId);
    List<InspectionUrlDto> retrieveChildUrl(Long memberId, Long parentUrlId);

    InspectionUrlDto retrieveUrl(Long urlId, Long memberId);

    InspectionUrlDto updateUrl(InspectionUrlDto.Request requestDto, Long urlId);

    String deleteUrl(Long urlId, Long memberId);

    boolean validateUrl(String url);
}
