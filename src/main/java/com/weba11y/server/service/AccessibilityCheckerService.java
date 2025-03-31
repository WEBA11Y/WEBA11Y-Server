package com.weba11y.server.service;


import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlDto;
import reactor.core.publisher.Flux;

public interface AccessibilityCheckerService {

    Flux<InspectionResultDto> runChecks(InspectionUrlDto inspectionUrl);
}
