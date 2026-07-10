package io.github.khghouse.petwellness.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import io.github.khghouse.petwellness.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class MemberAuthUserReaderTest extends IntegrationTestSupport {

    @Autowired private MemberAuthUserReader memberAuthUserReader;

    @Autowired private MemberService memberService;

    @Autowired private MemberRepository memberRepository;

    @DisplayName("삭제되지 않은 회원이면 이메일로 인증 사용자 정보를 조회한다")
    @Test
    void findByUsername_activeMember_returnsAuthUser() {
        // given
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        Member member = memberRepository.findByEmail("member@example.com").orElseThrow();

        // when
        var authUser = memberAuthUserReader.findByUsername("member@example.com").orElseThrow();

        // then
        assertThat(authUser.id()).isEqualTo(member.getId());
        assertThat(authUser.username()).isEqualTo("member@example.com");
        assertThat(authUser.password()).isEqualTo(member.getPassword());
        assertThat(authUser.authorities()).containsExactly("ROLE_MEMBER");
    }

    @DisplayName("탈퇴한 회원이면 이메일로 인증 사용자 정보를 조회하지 않는다")
    @Test
    void findByUsername_withdrawnMember_returnsEmpty() {
        // given
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        Member member = memberRepository.findByEmail("member@example.com").orElseThrow();
        memberService.withdraw(member.getId());

        // when
        var authUser = memberAuthUserReader.findByUsername("member@example.com");

        // then
        assertThat(authUser).isEmpty();
    }

    @DisplayName("탈퇴한 회원이면 회원 식별자로 인증 사용자 정보를 조회하지 않는다")
    @Test
    void findById_withdrawnMember_returnsEmpty() {
        // given
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        Member member = memberRepository.findByEmail("member@example.com").orElseThrow();
        memberService.withdraw(member.getId());

        // when
        var authUser = memberAuthUserReader.findById(member.getId());

        // then
        assertThat(authUser).isEmpty();
    }
}
