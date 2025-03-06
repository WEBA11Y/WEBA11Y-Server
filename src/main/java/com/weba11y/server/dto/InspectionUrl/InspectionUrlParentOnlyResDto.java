package com.weba11y.server.dto.InspectionUrl;

import com.weba11y.server.domain.enums.InspectionStatus;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionUrlParentOnlyResDto {
    private Long id;
    private String summary;
    private String url;
    private InspectionStatus status;
}
