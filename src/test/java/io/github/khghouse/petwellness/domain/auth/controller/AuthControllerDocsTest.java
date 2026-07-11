package io.github.khghouse.petwellness.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
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

import io.github.khghouse.common.auth.domain.auth.controller.AuthController;
import io.github.khghouse.common.auth.domain.auth.dto.request.LoginRequest;
import io.github.khghouse.common.auth.domain.auth.dto.request.ReissueRequest;
import io.github.khghouse.common.auth.domain.auth.dto.response.TokenResponse;
import io.github.khghouse.common.auth.domain.auth.service.AuthService;
import io.github.khghouse.common.auth.global.security.AuthPrincipal;
import io.github.khghouse.petwellness.support.RestDocsSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class AuthControllerDocsTest extends RestDocsSupport {

    private final AuthService authService = Mockito.mock(AuthService.class);

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @DisplayName("JWT 로그인 API를 문서화한다")
    @Test
    void login_validRequest_generatesRestDocs() throws Exception {
        LoginRequest request = new LoginRequest("member@example.com", "password1");
        given(authService.login(any()))
                .willReturn(TokenResponse.of("access-token", "refresh-token"));

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                requestFields(
                                        fieldWithPath("loginId").type(STRING).description("회원 이메일"),
                                        fieldWithPath("password").type(STRING).description("비밀번호")),
                                tokenResponseFields()));
    }

    @DisplayName("JWT 토큰 재발급 API를 문서화한다")
    @Test
    void reissue_validRequest_generatesRestDocs() throws Exception {
        ReissueRequest request = new ReissueRequest("refresh-token");
        given(authService.reissue("refresh-token"))
                .willReturn(TokenResponse.of("new-access-token", "new-refresh-token"));

        mockMvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                requestFields(
                                        fieldWithPath("refreshToken")
                                                .type(STRING)
                                                .description("Refresh Token")),
                                tokenResponseFields()));
    }

    @DisplayName("로그아웃 API를 문서화한다")
    @Test
    void logout_validRequest_generatesRestDocs() throws Exception {
        mockMvc.perform(
                        post("/api/auth/logout")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                                .principal(authenticatedMember()))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION)
                                                .description("Bearer Access Token")),
                                responseFields(
                                        fieldWithPath("status")
                                                .type(NUMBER)
                                                .description("HTTP 상태 코드"),
                                        fieldWithPath("success")
                                                .type(BOOLEAN)
                                                .description("요청 성공 여부"))));
    }

    private org.springframework.restdocs.payload.ResponseFieldsSnippet tokenResponseFields() {
        return responseFields(
                fieldWithPath("status").type(NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                fieldWithPath("data.accessToken").type(STRING).description("Access Token"),
                fieldWithPath("data.refreshToken").type(STRING).description("Refresh Token"));
    }

    private Authentication authenticatedMember() {
        AuthPrincipal principal = AuthPrincipal.authenticated(1L, List.of("ROLE_MEMBER"));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
