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
public class InspectionResultReactive extends BaseEntity {

    @Id
    @Column("result_id")
    private Long id;

    private int inspectionItems;

    private String summary;

    private String codeLine;

    @Column("inspection_url_id")
    private Long inspectionUrlId;

    @Column("create_date")
    private LocalDateTime createDate;

    @Column("update_date")
    private LocalDateTime updateDate;

    @Column("delete_date")
    private LocalDateTime deleteDate;


    @Builder
    public InspectionResultReactive(int inspectionItems, String summary, String codeLine, Long inspectionUrlId) {
        this.inspectionItems = inspectionItems;
        this.summary = summary;
        this.codeLine = codeLine;
        this.inspectionUrlId = inspectionUrlId;
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
        this.deleteDate = null;
    }

    public InspectionResultDto toDto() {
        return InspectionResultDto.builder()
                .inspectionItems(InspectionItems.findByNumber(this.inspectionItems))
                .summary(this.summary)
                .codeLine(this.codeLine)
                .build();
    }
}
