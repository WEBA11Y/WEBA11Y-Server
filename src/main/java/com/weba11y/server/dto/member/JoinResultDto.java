package com.weba11y.server.dto.member;


import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinResultDto {
    private String message;
    private Long id;
    private String userId;
}
