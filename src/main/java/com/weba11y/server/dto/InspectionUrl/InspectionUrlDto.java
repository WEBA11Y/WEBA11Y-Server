package com.weba11y.server.dto.InspectionUrl;

import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.Member;
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
    private String summary;
    private String url;
    private String favicon;
    private Long parentId;
    @Builder.Default
    private List<InspectionUrlDto> child = new ArrayList<>();
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;

    public InspectionUrlDto.Response toResponse(){
        return Response.builder()
                .id(this.id)
                .summary(this.summary)
                .url(this.url)
                .favicon(this.favicon)
                .parentId(this.parentId != null ? this.parentId : null)
                .child(this.child != null ? this.child.stream()
                        .map(InspectionUrlDto::toResponse)
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
        private String summary;

        @NotNull(message = "URL을 입력하세요.")
        @Size(min = 10, max = 2048)
        @Pattern(regexp = "^(http|https)://.*$", message = "URL은 http:// 또는 https:// 로 시작해야합니다.")
        private String url;

        private Long parentId;

        // 부모 URL이 있는 경우
        public InspectionUrl toEntity(InspectionUrl parent, Member member) {
            InspectionUrl inspectionUrl = InspectionUrl.builder()
                    .summary(this.summary)
                    .url(this.url)
                    .member(member)
                    .build();
            inspectionUrl.addParentUrl(parent);
            return inspectionUrl;
        }

        public InspectionUrl toEntity(Member member) {
            return InspectionUrl.builder()
                    .summary(this.summary)
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
        private String summary;
        private String url;
        private String favicon;
        private Long parentId;
        @Builder.Default
        private List<InspectionUrlDto.Response> child = new ArrayList<>();
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ParentOnlyResponse {
        private Long id;
        private String summary;
        private String url;
        private String favicon;
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
    }
}
