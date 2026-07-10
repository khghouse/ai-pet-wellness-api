package io.github.khghouse.petwellness.domain.member.service;

import io.github.khghouse.common.auth.global.auth.AuthUser;
import io.github.khghouse.common.auth.global.auth.AuthUserReader;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberAuthUserReader implements AuthUserReader {

    private static final String MEMBER_AUTHORITY = "ROLE_MEMBER";

    private final MemberRepository memberRepository;

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        return memberRepository
                .findByEmail(username)
                .filter(member -> !member.isDeleted())
                .map(this::toAuthUser);
    }

    @Override
    public Optional<AuthUser> findById(Long userId) {
        return memberRepository
                .findById(userId)
                .filter(member -> !member.isDeleted())
                .map(this::toAuthUser);
    }

    private AuthUser toAuthUser(Member member) {
        return new AuthUser(
                member.getId(), member.getEmail(), member.getPassword(), List.of(MEMBER_AUTHORITY));
    }
}
