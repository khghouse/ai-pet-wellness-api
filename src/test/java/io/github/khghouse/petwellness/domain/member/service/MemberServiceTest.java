package io.github.khghouse.petwellness.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.exception.MemberErrorCode;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import io.github.khghouse.petwellness.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class MemberServiceTest extends IntegrationTestSupport {

    @Autowired private MemberService memberService;

    @Autowired private MemberRepository memberRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @DisplayName("정상 입력이면 회원을 생성하고 비밀번호를 단방향 해시로 저장한다")
    @Test
    void signup_validRequest_persistsMemberWithEncodedPassword() {
        // given
        MemberSignupServiceRequest request =
                new MemberSignupServiceRequest("member@example.com", "password1");

        // when
        memberService.signup(request);

        // then
        Member member = memberRepository.findByEmail("member@example.com").orElseThrow();
        assertThat(member.getEmail()).isEqualTo("member@example.com");
        assertThat(member.getPassword()).isNotEqualTo("password1");
        assertThat(passwordEncoder.matches("password1", member.getPassword())).isTrue();
        assertThat(member.isDeleted()).isFalse();
        assertThat(member.getDeletedAt()).isNull();
    }

    @DisplayName("이미 가입된 이메일이면 회원 가입에 실패한다")
    @Test
    void signup_duplicateEmail_throwsEmailDuplicated() {
        // given
        MemberSignupServiceRequest request =
                new MemberSignupServiceRequest("member@example.com", "password1");
        memberService.signup(request);

        // when & then
        assertThatThrownBy(() -> memberService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.EMAIL_DUPLICATED);
    }
}
