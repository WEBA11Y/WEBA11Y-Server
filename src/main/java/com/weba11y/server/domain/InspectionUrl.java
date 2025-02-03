package com.weba11y.server.domain;


import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.InspectionStatus;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
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
    private String summary;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    private InspectionStatus status = InspectionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private InspectionUrl parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @BatchSize(size = 10)
    private Set<InspectionUrl> child = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public InspectionUrl(String summary, String url, Member member) {
        this.summary = summary;
        this.url = url;
        this.member = member;
    }

    public void addChildUrl(InspectionUrl child) {
        this.child.add(child);
    }

    public void addParentUrl(InspectionUrl parent) {
        this.parent = parent;
        parent.addChildUrl(this);
    }

    public void updateStatus(InspectionStatus status) {
        this.status = status;
    }

    public void removeChildUrl(InspectionUrl child) {
        if (this.child.contains(child)) {
            this.child.remove(child);
        } else {
            throw new IllegalArgumentException("Child not found in the list");
        }
    }

    public InspectionUrlResponseDto toDto() {
        return InspectionUrlResponseDto.builder()
                .id(this.id)
                .summary(this.summary)
                .url(this.url)
                .status(this.status)
                .parentId(this.parent != null ? this.parent.getId() : null)
                .child(this.child != null ? this.child.stream()
                        .map(InspectionUrl::toDto)
                        .toList() : null)
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }

}
