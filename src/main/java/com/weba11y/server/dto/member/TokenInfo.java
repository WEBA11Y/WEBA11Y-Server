package com.weba11y.server.dto.member;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    private Long memberId;
    private String role;
}
