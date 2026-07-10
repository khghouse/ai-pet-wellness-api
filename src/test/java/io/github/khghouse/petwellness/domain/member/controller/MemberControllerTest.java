package io.github.khghouse.petwellness.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberLoginRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.dto.response.MemberResponse;
import io.github.khghouse.petwellness.domain.member.entity.MemberStatus;
import io.github.khghouse.petwellness.domain.member.exception.MemberErrorCode;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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

    @DisplayName("정상 입력이면 로그인에 성공하고 비밀번호를 응답하지 않는다")
    @Test
    void login_validRequest_returnsMemberWithoutPassword() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest("member@example.com", "password1");
        given(memberService.login(any()))
                .willReturn(new MemberResponse(1L, "member@example.com", MemberStatus.ACTIVE));

        mockMvc.perform(
                        post("/api/v1/members/login")
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

    @DisplayName("이메일 형식이 올바르지 않으면 로그인에 실패한다")
    @Test
    void login_invalidEmail_returnsBadRequest() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest("invalid-email", "password1");

        mockMvc.perform(
                        post("/api/v1/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("필수 입력값이 누락되면 로그인에 실패한다")
    @Test
    void login_missingRequiredField_returnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/members/login")
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

    @DisplayName("로그인 인증에 실패하면 LOGIN_FAILED 응답을 반환한다")
    @Test
    void login_authenticationFailed_returnsLoginFailed() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest("member@example.com", "password1");
        given(memberService.login(any()))
                .willThrow(new CustomException(MemberErrorCode.LOGIN_FAILED));

        mockMvc.perform(
                        post("/api/v1/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("LOGIN_FAILED"));
    }

    @DisplayName("회원 탈퇴에 성공하면 응답 데이터 없이 성공 응답을 반환한다")
    @Test
    void withdraw_validRequest_returnsSuccessWithoutData() throws Exception {
        mockMvc.perform(delete("/api/v1/members/{memberId}", 1L))
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

        mockMvc.perform(delete("/api/v1/members/{memberId}", 1L))
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

        mockMvc.perform(delete("/api/v1/members/{memberId}", 1L))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_ALREADY_WITHDRAWN"));
    }
}
