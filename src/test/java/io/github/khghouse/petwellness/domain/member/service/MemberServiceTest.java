package io.github.khghouse.petwellness.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberLoginServiceRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.entity.MemberStatus;
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

    @DisplayName("이메일과 비밀번호가 일치하면 로그인에 성공한다")
    @Test
    void login_validRequest_returnsMember() {
        // given
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        MemberLoginServiceRequest request =
                new MemberLoginServiceRequest("member@example.com", "password1");

        // when
        var response = memberService.login(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("member@example.com");
        assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE);
    }

    @DisplayName("존재하지 않는 이메일이면 로그인에 실패한다")
    @Test
    void login_notFoundEmail_throwsLoginFailed() {
        // given
        MemberLoginServiceRequest request =
                new MemberLoginServiceRequest("unknown@example.com", "password1");

        // when & then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.LOGIN_FAILED);
    }

    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    @Test
    void login_passwordMismatch_throwsLoginFailed() {
        // given
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        MemberLoginServiceRequest request =
                new MemberLoginServiceRequest("member@example.com", "password2");

        // when & then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.LOGIN_FAILED);
    }

    @DisplayName("탈퇴한 회원이면 로그인에 실패한다")
    @Test
    void login_deletedMember_throwsLoginFailed() {
        // given
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        Member member = memberRepository.findByEmail("member@example.com").orElseThrow();
        member.withdraw();

        MemberLoginServiceRequest request =
                new MemberLoginServiceRequest("member@example.com", "password1");

        // when & then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.LOGIN_FAILED);
    }

    @DisplayName("비활성 상태의 회원이면 로그인에 실패한다")
    @Test
    void login_inactiveMember_throwsLoginFailed() {
        // given
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        Member member = memberRepository.findByEmail("member@example.com").orElseThrow();
        member.deactivate();

        MemberLoginServiceRequest request =
                new MemberLoginServiceRequest("member@example.com", "password1");

        // when & then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.LOGIN_FAILED);
    }
}
