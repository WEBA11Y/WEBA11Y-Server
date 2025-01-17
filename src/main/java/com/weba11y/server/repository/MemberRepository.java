package com.weba11y.server.repository;

import com.weba11y.server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNum(String phoneNum);
}
