package com.weba11y.server.service;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;

public interface InspectionUrlService {

    InspectionUrlResponseDto saveUrl(InspectionUrlRequestDto dto, Member member);
}
