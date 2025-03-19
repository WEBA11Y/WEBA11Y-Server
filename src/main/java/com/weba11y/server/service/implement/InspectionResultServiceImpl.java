package com.weba11y.server.service.implement;

import com.weba11y.server.domain.InspectionResult;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import com.weba11y.server.repository.InspectionResultRepository;
import com.weba11y.server.service.InspectionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionResultServiceImpl implements InspectionResultService {

    private final InspectionResultRepository inspectionResultRepository;

    @Override
    public List<LocalDate> retrieveInspectionResultDateByUrlId(Long urlId) {
        try {
            return inspectionResultRepository.findCreateDatesByInspectionUrlId(urlId);
        }catch (Exception e){
            log.error("기록 날짜 가져오다 실패함 , {}", e.getMessage());
            return null;
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
