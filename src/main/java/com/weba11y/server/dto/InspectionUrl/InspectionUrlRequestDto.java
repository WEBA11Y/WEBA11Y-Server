package com.weba11y.server.dto.InspectionUrl;


import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.Member;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionUrlRequestDto {
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
