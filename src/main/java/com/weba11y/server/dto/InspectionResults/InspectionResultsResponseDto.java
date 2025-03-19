package com.weba11y.server.dto.InspectionResults;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultsResponseDto {

    private static final int NUM_INSPECTION_ITEMS = 33;

    private int[] status = new int[NUM_INSPECTION_ITEMS];

    private List<InspectionResultDto> resultList = new ArrayList<>();

   /* public void addResult(InspectionResultDto inspectionResultDto){
        this.resultList.add(inspectionResultDto);
        status[inspectionResultDto.getNumber()] = 1;
    }*/
}
