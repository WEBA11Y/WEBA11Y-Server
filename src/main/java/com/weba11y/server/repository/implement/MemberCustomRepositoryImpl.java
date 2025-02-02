package com.weba11y.server.repository.implement;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.repository.MemberCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {
    private final EntityManager em;

    @Override
    public Optional<Member> findByUserId(String userId) {
        try {
            Member member = em.createQuery("select m from Member m where m.userId =: userId", Member.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return Optional.of(member);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsUniqueInfo(JoinDto joinDto) {
        Long count = em.createQuery("select count(m) from Member m " +
                                "where m.userId =:userId or m.phoneNum =: phoneNum"
                        , Long.class)
                .setParameter("userId", joinDto.getUserId())
                .setParameter("phoneNum", joinDto.getPhoneNum())
                .getSingleResult();
        return count == 0;
    }
}
