package com.weba11y.server.domain;


import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.InspectionStatus;
import com.weba11y.server.dto.inspectionUrl.InspectionUrlDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionUrl extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 2048)
    private String url;

    private String favicon;

    @Enumerated(EnumType.STRING)
    private InspectionStatus status = InspectionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private InspectionUrl parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<InspectionUrl> child = new HashSet<>();

    @OneToMany(mappedBy = "inspectionUrl", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<InspectionSummary> inspectionSummaries = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public InspectionUrl(String description, String url, Member member, String favicon) {
        this.description = description;
        this.url = url;
        this.member = member;
    }

    public void addFavicon(String favicon) {
        this.favicon = favicon;
    }

    public void addChildUrl(InspectionUrl child) {
        this.child.add(child);
    }

    public void addParentUrl(InspectionUrl parent) {
        this.parent = parent;
        parent.addChildUrl(this);
    }

    public void addSummary(InspectionSummary inspectionSummary){
        this.inspectionSummaries.add(inspectionSummary);
    }

    public void updateStatus(InspectionStatus status) {
        this.status = status;
    }

    public void delete() {
        status = InspectionStatus.HIDE;
        if (!child.isEmpty())
            child.stream().forEach(child -> child.delete());
        onDelete();
    }

    public void removeChildUrl(InspectionUrl child) {
        if (this.child.contains(child)) {
            this.child.remove(child);
        } else {
            throw new IllegalArgumentException("Child not found in the list");
        }
    }

    public InspectionUrlDto toDto() {
        return InspectionUrlDto.builder()
                .id(this.id)
                .description(this.description)
                .url(this.url)
                .favicon(this.favicon)
                .parentId(this.parent != null ? this.parent.getId() : null)
                .child(this.child != null ? this.child.stream()
                        .map(InspectionUrl::toDto)
                        .toList() : null)
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .deleteDate(this.getDeleteDate())
                .build();
    }

    public InspectionUrlDto.Parent toParentDto() {
        return InspectionUrlDto.Parent.builder()
                .id(this.id)
                .description(this.description)
                .url(this.url)
                .favicon(this.favicon)
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }

    public void update(InspectionUrlDto.Request requestDto) {
        this.description = requestDto.getDescription();
        this.url = requestDto.getUrl();
        onUpdate();
    }
}
