package com.weba11y.server.api.dto.inspectionUrl;

import com.weba11y.server.domain.inspection.url.InspectionUrl;
import com.weba11y.server.domain.member.Member;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionUrlDto {

    private Long id;
    private String description;
    private String url;
    private String favicon;
    private Long parentId;
    @Builder.Default
    private List<InspectionUrlDto> child = new ArrayList<>();
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;

    public InspectionUrlDto.Response toResponse() {
        return Response.builder()
                .id(this.id)
                .description(this.description)
                .url(this.url)
                .favicon(this.favicon)
                .parentId(this.parentId != null ? this.parentId : null)
                .childUrls(this.child != null ? this.child.stream()
                        .map(child -> Response.ChildUrl.builder()
                                .id(child.id)
                                .description(child.description)
                                .url(child.url)
                                .favicon(child.favicon)
                                .createDate(child.createDate)
                                .updateDate(child.updateDate)
                                .build())
                        .toList() : null)
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @NotNull(message = "설명은 최소 2글자 최대 255자로 입력해야합니다.")
        @Size(min = 2, max = 255, message = "설명은 최소 2글자 최대 255자로 입력해야합니다.")
        private String description;

        @NotNull(message = "URL을 입력하세요.")
        @Size(min = 10, max = 2048)
        @Pattern(regexp = "^(http|https)://.*$", message = "URL은 http:// 또는 https:// 로 시작해야합니다.")
        private String url;

        private Long parentId;

        // 부모 URL이 있는 경우
        public InspectionUrl toEntity(InspectionUrl parent, Member member) {
            InspectionUrl inspectionUrl = InspectionUrl.builder()
                    .description(this.description)
                    .url(this.url)
                    .member(member)
                    .build();
            inspectionUrl.addParentUrl(parent);
            return inspectionUrl;
        }

        public InspectionUrl toEntity(Member member) {
            return InspectionUrl.builder()
                    .description(this.description)
                    .url(this.url)
                    .member(member)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Response {
        private Long id;
        private String description;
        private String url;
        private String favicon;
        private Long parentId;
        @Builder.Default
        private List<ChildUrl> childUrls = new ArrayList<>();
        private LocalDateTime createDate;
        private LocalDateTime updateDate;

        @Getter
        @Builder
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        @AllArgsConstructor(access = AccessLevel.PROTECTED)
        public static class ChildUrl {
            private Long id;
            private String description;
            private String url;
            private String favicon;
            private LocalDateTime createDate;
            private LocalDateTime updateDate;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ParentOnlyResponse {
        @Builder.Default
        private List<Parent> content = new ArrayList<>();
        private int totalPage;
        private int currentPage;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Parent {
        private Long id;
        private String description;
        private String url;
        private String favicon;
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
    }
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChildUrlResponse {
        private Long id;
        private String description;
        private String url;
        private String favicon;
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
    }
}