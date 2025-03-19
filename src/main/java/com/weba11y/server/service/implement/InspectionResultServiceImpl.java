package com.weba11y.server.service.implement;

import com.weba11y.server.domain.InspectionResult;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import com.weba11y.server.repository.InspectionResultRepository;
import com.weba11y.server.service.InspectionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionResultServiceImpl implements InspectionResultService {

    private final InspectionResultRepository inspectionResultRepository;

    @Override
    public List<LocalDate> retrieveInspectionResultDateByUrlId(Long urlId) {
        try {
            return Optional.ofNullable(inspectionResultRepository.findCreateDatesByInspectionUrlId(urlId))
                    .orElseGet(Collections::emptyList);
        } catch (Exception e) {
            log.error("기록 날짜 가져오는 중 오류 발생 - urlId: {}, error: ", urlId, e);
            return Collections.emptyList();
        }
    }



    @Override
    public List<InspectionResultDto> retrieveResultsByUrlIdAndDate(Long urlId, LocalDate date) {
        return inspectionResultRepository.findInspectionResultsByUrlIdAndCreateDate(urlId, date)
                .stream()
                .map(InspectionResult::toDto)
                .collect(Collectors.toList());
    }
}
