package io.github.khghouse.petwellness.domain.member.service;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.dto.response.MemberResponse;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.exception.MemberErrorCode;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signup(MemberSignupServiceRequest request) {
        validateEmailNotDuplicated(request.email());

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = Member.create(request.email(), encodedPassword);

        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateMemberReadable(member);

        return MemberResponse.from(member);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateNotWithdrawn(member);

        member.withdraw();
    }

    private void validateEmailNotDuplicated(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(MemberErrorCode.EMAIL_DUPLICATED);
        }
    }

    private void validateNotWithdrawn(Member member) {
        if (member.isWithdrawn()) {
            throw new CustomException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
    }

    private void validateMemberReadable(Member member) {
        if (member.isWithdrawn()) {
            throw new CustomException(MemberErrorCode.MEMBER_WITHDRAWN);
        }
    }
}
