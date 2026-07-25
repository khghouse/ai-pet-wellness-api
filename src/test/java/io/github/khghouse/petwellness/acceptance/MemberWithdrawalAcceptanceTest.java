package io.github.khghouse.petwellness.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.khghouse.common.auth.domain.auth.dto.request.LoginRequest;
import io.github.khghouse.common.auth.domain.auth.dto.request.ReissueRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class MemberWithdrawalAcceptanceTest {

    private static final int REDIS_PORT = 6379;

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
                    .withExposedPorts(REDIS_PORT);

    @LocalServerPort private int port;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private MemberRepository memberRepository;

    @Autowired private StringRedisTemplate stringRedisTemplate;

    private RestClient restClient;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
        registry.add("spring.data.redis.username", () -> "");
        registry.add("spring.data.redis.password", () -> "");
    }

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        clearTestData();
    }

    @AfterEach
    void tearDown() {
        clearTestData();
    }

    @DisplayName("회원 탈퇴하면 로그인과 기존 토큰 사용에 실패한다")
    @Test
    void memberWithdrawal_revokesAccessAndRefreshTokens() throws Exception {
        ResponseEntity<String> signupResponse =
                post(
                        "/api/v1/members",
                        new MemberSignupRequest("withdrawal-scenario@example.com", "password1"));

        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> loginResponse =
                post(
                        "/api/auth/login",
                        new LoginRequest("withdrawal-scenario@example.com", "password1"));

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode loginData = getSuccessData(loginResponse);
        String accessToken = loginData.get("accessToken").asText();
        String refreshToken = loginData.get("refreshToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        ResponseEntity<String> withdrawalResponse = delete("/api/v1/members/me", accessToken);

        assertThat(withdrawalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponseBody(withdrawalResponse).get("data").isNull()).isTrue();

        ResponseEntity<String> withdrawnLoginResponse =
                post(
                        "/api/auth/login",
                        new LoginRequest("withdrawal-scenario@example.com", "password1"));

        assertErrorResponse(withdrawnLoginResponse, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");

        ResponseEntity<String> getMeResponse = get("/api/v1/members/me", accessToken);

        assertErrorResponse(getMeResponse, HttpStatus.UNAUTHORIZED, "TOKEN_BLACKLISTED");

        ResponseEntity<String> reissueResponse =
                post("/api/auth/reissue", new ReissueRequest(refreshToken));

        assertErrorResponse(reissueResponse, HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND");
    }

    private void clearTestData() {
        stringRedisTemplate.execute(
                (RedisCallback<Void>)
                        connection -> {
                            connection.serverCommands().flushDb();
                            return null;
                        });
        memberRepository.deleteAll();
    }

    private ResponseEntity<String> post(String uri, Object request) throws Exception {
        return restClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .toEntity(String.class);
    }

    private ResponseEntity<String> delete(String uri, String accessToken) {
        return restClient
                .delete()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(String.class);
    }

    private ResponseEntity<String> get(String uri, String accessToken) {
        return restClient
                .get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(String.class);
    }

    private JsonNode getSuccessData(ResponseEntity<String> response) throws Exception {
        JsonNode body = getResponseBody(response);
        assertThat(body.get("success").asBoolean()).isTrue();
        return body.get("data");
    }

    private void assertErrorResponse(
            ResponseEntity<String> response, HttpStatus expectedStatus, String expectedErrorCode)
            throws Exception {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        JsonNode body = getResponseBody(response);
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("error").get("code").asText()).isEqualTo(expectedErrorCode);
    }

    private JsonNode getResponseBody(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }
}
