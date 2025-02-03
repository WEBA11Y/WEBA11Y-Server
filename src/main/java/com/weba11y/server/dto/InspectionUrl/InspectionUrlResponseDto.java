package com.weba11y.server.dto.InspectionUrl;


import com.weba11y.server.domain.enums.InspectionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionUrlResponseDto {
    private Long id;
    private String summary;
    private String url;
    private InspectionStatus status;
    private Long parentId;
    @Builder.Default
    private List<InspectionUrlResponseDto> child = new ArrayList<>();
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}


