package io.github.khghouse.petwellness.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.petwellness.domain.member.dto.request.MemberLoginRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.dto.response.MemberResponse;
import io.github.khghouse.petwellness.domain.member.entity.MemberStatus;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

class MemberControllerDocsTest extends RestDocsSupport {

    private final MemberService memberService = Mockito.mock(MemberService.class);

    @Override
    protected Object initController() {
        return new MemberController(memberService);
    }

    @DisplayName("회원 가입 API를 문서화한다")
    @Test
    void signup_validRequest_generatesRestDocs() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("member@example.com", "password1");
        given(memberService.signup(any()))
                .willReturn(new MemberResponse(1L, "member@example.com", MemberStatus.ACTIVE));

        mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                requestFields(
                                        fieldWithPath("email").type(STRING).description("이메일"),
                                        fieldWithPath("password").type(STRING).description("비밀번호")),
                                responseFields(
                                        fieldWithPath("status")
                                                .type(NUMBER)
                                                .description("HTTP 상태 코드"),
                                        fieldWithPath("success")
                                                .type(BOOLEAN)
                                                .description("요청 성공 여부"),
                                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.id").type(NUMBER).description("회원 식별자"),
                                        fieldWithPath("data.email").type(STRING).description("이메일"),
                                        fieldWithPath("data.status")
                                                .type(STRING)
                                                .description("회원 상태"))));
    }

    @DisplayName("로그인 API를 문서화한다")
    @Test
    void login_validRequest_generatesRestDocs() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest("member@example.com", "password1");
        given(memberService.login(any()))
                .willReturn(new MemberResponse(1L, "member@example.com", MemberStatus.ACTIVE));

        mockMvc.perform(
                        post("/api/v1/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                requestFields(
                                        fieldWithPath("email").type(STRING).description("이메일"),
                                        fieldWithPath("password").type(STRING).description("비밀번호")),
                                responseFields(
                                        fieldWithPath("status")
                                                .type(NUMBER)
                                                .description("HTTP 상태 코드"),
                                        fieldWithPath("success")
                                                .type(BOOLEAN)
                                                .description("요청 성공 여부"),
                                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.id").type(NUMBER).description("회원 식별자"),
                                        fieldWithPath("data.email").type(STRING).description("이메일"),
                                        fieldWithPath("data.status")
                                                .type(STRING)
                                                .description("회원 상태"))));
    }
}
