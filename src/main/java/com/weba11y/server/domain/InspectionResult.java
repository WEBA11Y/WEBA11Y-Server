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

    @Column(nullable = false)
    private String codeLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_url_id")
    private InspectionUrl inspectionUrl;

    public InspectionResultDto toDto() {
        return InspectionResultDto.builder()
                .id(this.id)
                .inspectionItems(this.inspectionItems)
                .assessmentLevel(this.inspectionItems.getAssessmentLevel())
                .importance(this.inspectionItems.getImportance())
                .summary(this.summary)
                .codeLine(this.codeLine)
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }

}
