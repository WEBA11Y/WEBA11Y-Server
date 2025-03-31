package com.weba11y.server.domain;


import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

// R2DBC 용 Entity class
@Table(name = "inspection_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultReactive {

    @Id
    @Column("result_id")
    private Long id;

    private int inspectionItems;

    private String summary;

    private String codeLine;

    @Column("inspection_url_id")
    private Long inspectionUrlId;

    @Column("create_date")
    private String createDate;

    @Column("update_date")
    private String updateDate;

    @Column("delete_date")
    private String deleteDate;


    @Builder
    public InspectionResultReactive(int inspectionItems, String summary, String codeLine, Long inspectionUrlId, String createDate, String updateDate) {
        this.inspectionItems = inspectionItems;
        this.summary = summary;
        this.codeLine = codeLine;
        this.inspectionUrlId = inspectionUrlId;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.deleteDate = null;
    }

    public InspectionResultDto toDto() {
        InspectionItems item = InspectionItems.findByNumber(this.inspectionItems);
        return InspectionResultDto.builder()
                .id(this.id)
                .inspectionItems(item)
                .assessmentLevel(item.getAssessmentLevel())
                .importance(item.getImportance())
                .summary(this.summary)
                .codeLine(this.codeLine)
                .createDate(LocalDateTime.parse(this.createDate))
                .updateDate(LocalDateTime.parse(this.updateDate))
                .build();
    }
}
