package com.weba11y.server.service;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;

import java.util.List;

public interface InspectionUrlService {

    InspectionUrlResponseDto saveUrl(InspectionUrlRequestDto dto, Member member);

    List<InspectionUrlResponseDto> retrieveAll(Long memberId);

    List<InspectionUrlResponseDto> retrieveParentUrl(Long memberId);
    List<InspectionUrlResponseDto> retrieveChildUrl(Long memberId, Long parentUrlId);

    InspectionUrlResponseDto retrieveUrl(Long urlId, Long memberId);

    InspectionUrlResponseDto updateUrl(InspectionUrlRequestDto requestDto, Long urlId);

    String deleteUrl(Long urlId, Long memberId);

    boolean validateUrl(String url);
}
