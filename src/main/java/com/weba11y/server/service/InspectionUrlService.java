package com.weba11y.server.service;

import com.weba11y.server.dto.InspectionDetailDto;
import com.weba11y.server.dto.inspectionUrl.InspectionUrlDto;
import org.springframework.http.HttpStatus;

import java.util.List;

public interface InspectionUrlService {

    InspectionUrlDto saveUrl(InspectionUrlDto.Request request, Long member);

    InspectionDetailDto getInspectionUrlDetail(Long urlId, Long memberId);

    List<InspectionUrlDto.Response> retrieveAll(Long memberId);

    InspectionUrlDto.ParentOnlyResponse retrieveParentUrl(Long memberId, int page);

    List<InspectionUrlDto.ChildUrlResponse> retrieveAllChildUrl(Long urlId, Long memberId);

    List<InspectionUrlDto> retrieveChildUrl(Long memberId, Long parentUrlId);

    InspectionUrlDto retrieveUrl(Long urlId, Long memberId);

    InspectionUrlDto updateUrl(InspectionUrlDto.Request requestDto, Long urlId);

    HttpStatus deleteUrl(List<Long> urlId, Long memberId);
}

