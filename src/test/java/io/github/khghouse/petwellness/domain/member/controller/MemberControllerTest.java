package io.github.khghouse.petwellness.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.common.auth.global.security.AuthPrincipal;
import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.dto.response.MemberResponse;
import io.github.khghouse.petwellness.domain.member.entity.MemberStatus;
import io.github.khghouse.petwellness.domain.member.exception.MemberErrorCode;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.support.ControllerTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(MemberController.class)
class MemberControllerTest extends ControllerTestSupport {

    @MockitoBean private MemberService memberService;

    @DisplayName("정상 입력이면 회원 가입에 성공하고 비밀번호를 응답하지 않는다")
    @Test
    void signup_validRequest_returnsMemberWithoutPassword() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("member@example.com", "password1");
        given(memberService.signup(any()))
                .willReturn(new MemberResponse(1L, "member@example.com", MemberStatus.ACTIVE));

        mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("member@example.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @DisplayName("이메일 형식이 올바르지 않으면 회원 가입에 실패한다")
    @Test
    void signup_invalidEmail_returnsBadRequest() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("invalid-email", "password1");

        mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("필수 입력값이 누락되면 회원 가입에 실패한다")
    @Test
    void signup_missingRequiredField_returnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "email": "member@example.com"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("비밀번호 정책을 만족하지 않으면 회원 가입에 실패한다")
    @Test
    void signup_invalidPasswordLength_returnsBadRequest() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("member@example.com", "short");

        mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("존재하는 회원이면 민감 정보 없이 회원 정보를 조회한다")
    @Test
    void getMe_validRequest_returnsMemberWithoutSensitiveInformation() throws Exception {
        given(memberService.getMember(1L))
                .willReturn(new MemberResponse(1L, "member@example.com", MemberStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/members/me").principal(authenticatedMember()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("member@example.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.deleted").doesNotExist())
                .andExpect(jsonPath("$.data.deletedAt").doesNotExist());

        then(memberService).should().getMember(1L);
    }

    @DisplayName("존재하지 않는 회원이면 회원 정보 조회에 실패한다")
    @Test
    void getMe_notFoundMember_returnsNotFound() throws Exception {
        willThrow(new CustomException(MemberErrorCode.MEMBER_NOT_FOUND))
                .given(memberService)
                .getMember(1L);

        mockMvc.perform(get("/api/v1/members/me").principal(authenticatedMember()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_NOT_FOUND"));
    }

    @DisplayName("탈퇴한 회원이면 회원 정보 조회에 실패한다")
    @Test
    void getMe_withdrawnMember_returnsUnprocessableEntity() throws Exception {
        willThrow(new CustomException(MemberErrorCode.MEMBER_WITHDRAWN))
                .given(memberService)
                .getMember(1L);

        mockMvc.perform(get("/api/v1/members/me").principal(authenticatedMember()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_WITHDRAWN"));
    }

    @DisplayName("회원 탈퇴에 성공하면 응답 데이터 없이 성공 응답을 반환한다")
    @Test
    void withdraw_validRequest_returnsSuccessWithoutData() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me").principal(authenticatedMember()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(memberService).should().withdraw(1L);
    }

    @DisplayName("존재하지 않는 회원이면 회원 탈퇴에 실패한다")
    @Test
    void withdraw_notFoundMember_returnsNotFound() throws Exception {
        willThrow(new CustomException(MemberErrorCode.MEMBER_NOT_FOUND))
                .given(memberService)
                .withdraw(1L);

        mockMvc.perform(delete("/api/v1/members/me").principal(authenticatedMember()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_NOT_FOUND"));
    }

    @DisplayName("이미 탈퇴한 회원이면 회원 탈퇴에 실패한다")
    @Test
    void withdraw_alreadyWithdrawnMember_returnsUnprocessableEntity() throws Exception {
        willThrow(new CustomException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN))
                .given(memberService)
                .withdraw(1L);

        mockMvc.perform(delete("/api/v1/members/me").principal(authenticatedMember()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_ALREADY_WITHDRAWN"));
    }

    private Authentication authenticatedMember() {
        AuthPrincipal principal = AuthPrincipal.authenticated(1L, List.of("ROLE_MEMBER"));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
