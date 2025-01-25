package com.weba11y.server.dto.InspectionUrl;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionUrlResponseDto {
    private Long id;
    private String title;
    private String url;
    private Long parentId;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
