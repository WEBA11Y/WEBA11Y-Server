package com.weba11y.server.infrastructure.persistence;

import com.weba11y.server.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    boolean existsByUserId(String username);

    boolean existsByPhoneNum(String phoneNum);
}
