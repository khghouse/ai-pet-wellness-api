package io.github.khghouse.petwellness.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.khghouse.common.auth.domain.auth.dto.request.LoginRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationRequest;
import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.repository.BreedRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetMembershipRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetWeightRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
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
class MemberPetJourneyAcceptanceTest {

    private static final int REDIS_PORT = 6379;

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
                    .withExposedPorts(REDIS_PORT);

    @LocalServerPort private int port;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private MemberRepository memberRepository;

    @Autowired private BreedRepository breedRepository;

    @Autowired private PetRepository petRepository;

    @Autowired private PetWeightRepository petWeightRepository;

    @Autowired private PetMembershipRepository petMembershipRepository;

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
        petMembershipRepository.deleteAll();
        petWeightRepository.deleteAll();
        petRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @DisplayName("신규 회원이 가입부터 로그인과 반려견 등록까지 완료한다")
    @Test
    void memberPetJourney_completesSignupLoginGetMeAndPetRegistration() throws Exception {
        ResponseEntity<String> signupResponse =
                post(
                        "/api/v1/members",
                        new MemberSignupRequest("scenario@example.com", "password1"));

        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode signupData = getSuccessData(signupResponse);
        long memberId = signupData.get("id").asLong();
        assertThat(memberId).isPositive();
        assertThat(signupData.get("email").asText()).isEqualTo("scenario@example.com");
        assertThat(signupData.get("status").asText()).isEqualTo("ACTIVE");

        ResponseEntity<String> loginResponse =
                post("/api/auth/login", new LoginRequest("scenario@example.com", "password1"));

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode loginData = getSuccessData(loginResponse);
        String accessToken = loginData.get("accessToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(loginData.get("refreshToken").asText()).isNotBlank();

        ResponseEntity<String> getMeResponse = get("/api/v1/members/me", accessToken);

        assertThat(getMeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode memberData = getSuccessData(getMeResponse);
        assertThat(memberData.get("id").asLong()).isEqualTo(memberId);
        assertThat(memberData.get("email").asText()).isEqualTo("scenario@example.com");
        assertThat(memberData.get("status").asText()).isEqualTo("ACTIVE");

        Breed breed = findBreed("말티즈");
        PetRegistrationRequest petRegistrationRequest =
                new PetRegistrationRequest(
                        "초코",
                        LocalDate.of(2023, 1, 1),
                        Gender.FEMALE,
                        breed.getId(),
                        new BigDecimal("4.5"),
                        NeuteredStatus.NEUTERED);
        ResponseEntity<String> petRegistrationResponse =
                post("/api/v1/pets", petRegistrationRequest, accessToken);

        assertThat(petRegistrationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode petData = getSuccessData(petRegistrationResponse);
        assertThat(petData.get("id").asLong()).isPositive();
        assertThat(petData.get("name").asText()).isEqualTo("초코");
        assertThat(petData.get("birthDate").asText()).isEqualTo("2023-01-01");
        assertThat(petData.get("gender").asText()).isEqualTo("FEMALE");
        assertThat(petData.get("breed").get("id").asLong()).isEqualTo(breed.getId());
        assertThat(petData.get("breed").get("name").asText()).isEqualTo("말티즈");
        assertThat(petData.get("weight").asText()).isEqualTo("4.5");
        assertThat(petData.get("neuteredStatus").asText()).isEqualTo("NEUTERED");
        assertThat(petData.get("membershipRole").asText()).isEqualTo("OWNER");
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

    private ResponseEntity<String> post(String uri, Object request, String accessToken)
            throws Exception {
        return restClient
                .post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
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
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("success").asBoolean()).isTrue();
        return body.get("data");
    }

    private Breed findBreed(String name) {
        return breedRepository.findAll().stream()
                .filter(breed -> breed.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
