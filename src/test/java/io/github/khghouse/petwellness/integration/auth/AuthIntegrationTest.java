package io.github.khghouse.petwellness.integration.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.common.auth.domain.auth.dto.request.LoginRequest;
import io.github.khghouse.common.auth.domain.auth.dto.request.ReissueRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.entity.MemberStatus;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthIntegrationTest extends IntegrationTestSupport {

    private static final int REDIS_PORT = 6379;

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
                    .withExposedPorts(REDIS_PORT);

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private MemberService memberService;

    @Autowired private MemberRepository memberRepository;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
        registry.add("spring.data.redis.username", () -> "");
        registry.add("spring.data.redis.password", () -> "");
    }

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @DisplayName("회원가입 API는 Access Token 없이 접근할 수 있다")
    @Test
    void signup_withoutAccessToken_returnsSuccess() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("member@example.com", "password1");

        mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @DisplayName("이메일과 비밀번호가 일치하면 Access Token과 Refresh Token을 발급한다")
    @Test
    void login_validCredentials_returnsTokens() throws Exception {
        signup("member@example.com");

        TokenPair tokens = login("member@example.com", "password1");

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
    }

    @DisplayName("로그인 정보가 일치하지 않으면 INVALID_CREDENTIALS 응답을 반환한다")
    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        signup("member@example.com");
        LoginRequest request = new LoginRequest("member@example.com", "wrong-password");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @DisplayName("탈퇴한 회원이면 로그인에 실패한다")
    @Test
    void login_withdrawnMember_returnsUnauthorized() throws Exception {
        Member member = signup("member@example.com");
        memberService.withdraw(member.getId());
        LoginRequest request = new LoginRequest("member@example.com", "password1");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @DisplayName("Access Token이 없으면 보호된 API 호출에 실패한다")
    @Test
    void getMe_withoutAccessToken_returnsTokenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_MISSING"));
    }

    @DisplayName("유효한 Access Token이면 자신의 회원 정보를 조회한다")
    @Test
    void getMe_validAccessToken_returnsAuthenticatedMember() throws Exception {
        Member member = signup("member@example.com");
        TokenPair tokens = login("member@example.com", "password1");

        mockMvc.perform(
                        get("/api/v1/members/me")
                                .header(HttpHeaders.AUTHORIZATION, bearer(tokens.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.email").value("member@example.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @DisplayName("유효한 Refresh Token이면 기존 토큰을 폐기하고 새 토큰 쌍을 발급한다")
    @Test
    void reissue_validRefreshToken_rotatesTokens() throws Exception {
        Member member = signup("member@example.com");
        TokenPair original = login("member@example.com", "password1");

        TokenPair reissued = reissue(original.refreshToken());

        assertThat(reissued.accessToken()).isNotBlank();
        assertThat(reissued.refreshToken()).isNotBlank();
        assertThat(reissued.accessToken()).isNotEqualTo(original.accessToken());
        assertThat(reissued.refreshToken()).isNotEqualTo(original.refreshToken());

        mockMvc.perform(
                        get("/api/v1/members/me")
                                .header(HttpHeaders.AUTHORIZATION, bearer(reissued.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(member.getId()));

        ReissueRequest oldTokenRequest = new ReissueRequest(original.refreshToken());
        mockMvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(oldTokenRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_NOT_FOUND"));

        TokenPair rotatedAgain = reissue(reissued.refreshToken());
        assertThat(rotatedAgain.accessToken()).isNotBlank();
        assertThat(rotatedAgain.refreshToken()).isNotEqualTo(reissued.refreshToken());
    }

    @DisplayName("탈퇴한 회원이면 Refresh Token을 재발급할 수 없다")
    @Test
    void reissue_withdrawnMember_returnsUnauthorized() throws Exception {
        Member member = signup("member@example.com");
        TokenPair tokens = login("member@example.com", "password1");
        memberService.withdraw(member.getId());
        ReissueRequest request = new ReissueRequest(tokens.refreshToken());

        mockMvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_USER_NOT_FOUND"));
    }

    @DisplayName("로그아웃하면 기존 Access Token과 Refresh Token을 모두 사용할 수 없다")
    @Test
    void logout_validTokens_revokesAccessAndRefreshTokens() throws Exception {
        signup("member@example.com");
        TokenPair tokens = login("member@example.com", "password1");

        mockMvc.perform(
                        post("/api/auth/logout")
                                .header(HttpHeaders.AUTHORIZATION, bearer(tokens.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(
                        get("/api/v1/members/me")
                                .header(HttpHeaders.AUTHORIZATION, bearer(tokens.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_BLACKLISTED"));

        ReissueRequest request = new ReissueRequest(tokens.refreshToken());
        mockMvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_NOT_FOUND"));
    }

    @DisplayName("인증된 회원이 탈퇴하면 현재 Access Token과 Refresh Token을 즉시 폐기한다")
    @Test
    void withdraw_validAccessToken_revokesTokensAndWithdrawsAuthenticatedMember() throws Exception {
        Member member = signup("member@example.com");
        TokenPair tokens = login("member@example.com", "password1");

        mockMvc.perform(
                        delete("/api/v1/members/me")
                                .header(HttpHeaders.AUTHORIZATION, bearer(tokens.accessToken())))
                .andExpect(status().isOk());

        Member withdrawnMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(withdrawnMember.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(withdrawnMember.isDeleted()).isTrue();

        mockMvc.perform(
                        get("/api/v1/members/me")
                                .header(HttpHeaders.AUTHORIZATION, bearer(tokens.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_BLACKLISTED"));

        ReissueRequest request = new ReissueRequest(tokens.refreshToken());
        mockMvc.perform(
                        post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_NOT_FOUND"));
    }

    private Member signup(String email) {
        memberService.signup(new MemberSignupServiceRequest(email, "password1"));
        return memberRepository.findByEmail(email).orElseThrow();
    }

    private TokenPair login(String loginId, String password) throws Exception {
        LoginRequest request = new LoginRequest(loginId, password);
        MvcResult result =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andReturn();

        var data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return new TokenPair(data.get("accessToken").asText(), data.get("refreshToken").asText());
    }

    private TokenPair reissue(String refreshToken) throws Exception {
        ReissueRequest request = new ReissueRequest(refreshToken);
        MvcResult result =
                mockMvc.perform(
                                post("/api/auth/reissue")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andReturn();

        var data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return new TokenPair(data.get("accessToken").asText(), data.get("refreshToken").asText());
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record TokenPair(String accessToken, String refreshToken) {}
}
