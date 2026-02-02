package com.weba11y.server.infrastructure.persistence;

import com.weba11y.server.domain.member.Member;
import com.weba11y.server.api.dto.member.JoinDto;
import java.util.Optional;

public interface MemberCustomRepository {
    Optional<Member> findByUserId(String userId);

    boolean existsUniqueInfo(JoinDto joinDto);

}
