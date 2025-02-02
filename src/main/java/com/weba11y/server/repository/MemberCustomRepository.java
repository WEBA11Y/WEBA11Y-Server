package com.weba11y.server.repository;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.member.JoinDto;

import java.util.Optional;

public interface MemberCustomRepository {
    Optional<Member> findByUserId(String userId);

    boolean existsUniqueInfo(JoinDto joinDto);

}
