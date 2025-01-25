package com.weba11y.server.domain;


import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionUrl extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2048)
    private String url;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private InspectionUrl parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<InspectionUrl> child = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public InspectionUrl(String title, String url, Member member) {
        this.title = title;
        this.url = url;
        this.member = member;
    }

    public void addChildUrl(InspectionUrl child) {
        this.child.add(child);
    }

    public void addParentUrl(InspectionUrl parent) {
        this.parent = parent;
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
                .title(this.title)
                .url(this.url)
                .parentId(this.parent.getId())
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }
}
