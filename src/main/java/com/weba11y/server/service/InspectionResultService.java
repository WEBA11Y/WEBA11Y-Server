package com.weba11y.server.service;

import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;

import java.time.LocalDate;
import java.util.List;

public interface InspectionResultService {

    // URL ID 값으로 검사 한 날짜 가져오기
    List<LocalDate> retrieveInspectionResultDateByUrlId(Long urlId);

    // 해당 날짜의 모든 결과 가져오기
    List<InspectionResultDto> retrieveResultsByUrlIdAndDate(Long urlId, LocalDate date);

    // 해당 날짜 및 중요도 별로 가져오기
    InspectionResultDto.ResultListResponse retrieveResultsByDateAndImportance(int page, Long urlId, LocalDate date, Importance importance);

    // 해당 날짜 및 레벨 별로 가져오기
    InspectionResultDto.ResultListResponse retrieveResultsByDateAndLevel(int page, Long urlId, LocalDate date, AssessmentLevel assessmentLevel);

    // 선택한 검사 항목 결과만 가져오기
    //List<InspectionResultDto> retrieveResultsByItem();

    // 선택한 중요도 결과만 가져오기
    //List<InspectionResultDto> retrieveResultsByImportance(Importance importance);

    // 선택한 평가 수준의 결과만 가져오기
    //List<InspectionResultDto> retrieveResultsByAssessmentLevel(AssessmentLevel assessmentLevel);

}
