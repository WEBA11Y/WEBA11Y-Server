package com.weba11y.server.domain;

import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    @Column(nullable = false)
    private InspectionItems inspectionItems;

    @Column(nullable = false)
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    private InspectionUrl inspectionUrl;

    public InspectionResultDto toDto(){
        return InspectionResultDto.builder()
                .id(this.id)
                .inspectionItems(this.inspectionItems)
                .summary(this.summary)
                .build();
    }

}
