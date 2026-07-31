package io.github.khghouse.petwellness.domain.pet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.common.auth.global.security.AuthPrincipal;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationRequest;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetWeightRecordRequest;
import io.github.khghouse.petwellness.domain.pet.dto.response.BreedResponse;
import io.github.khghouse.petwellness.domain.pet.dto.response.MyPetResponse;
import io.github.khghouse.petwellness.domain.pet.dto.response.PetRegistrationResponse;
import io.github.khghouse.petwellness.domain.pet.dto.response.PetWeightRecordResponse;
import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import io.github.khghouse.petwellness.domain.pet.service.PetService;
import io.github.khghouse.petwellness.support.ControllerTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(PetController.class)
class PetControllerTest extends ControllerTestSupport {

    @MockitoBean private PetService petService;

    @DisplayName("인증된 회원이면 내 반려견 목록을 반환한다")
    @Test
    void getMyPets_authenticatedMember_returnsMyPetResponses() throws Exception {
        given(petService.getMyPets(any()))
                .willReturn(
                        List.of(
                                new MyPetResponse(
                                        1L,
                                        "초코",
                                        LocalDate.of(2023, 1, 1),
                                        PetMembershipRole.OWNER),
                                new MyPetResponse(
                                        2L,
                                        "보리",
                                        LocalDate.of(2022, 2, 2),
                                        PetMembershipRole.FAMILY)));

        mockMvc.perform(get("/api/v1/pets").principal(authenticatedMember()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("초코"))
                .andExpect(jsonPath("$.data[0].birthDate").value("2023-01-01"))
                .andExpect(jsonPath("$.data[0].membershipRole").value("OWNER"))
                .andExpect(jsonPath("$.data[0].gender").doesNotExist())
                .andExpect(jsonPath("$.data[0].breed").doesNotExist())
                .andExpect(jsonPath("$.data[0].weight").doesNotExist())
                .andExpect(jsonPath("$.data[0].neuteredStatus").doesNotExist())
                .andExpect(jsonPath("$.data[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.data[0].updatedAt").doesNotExist());
    }

    @DisplayName("정상 입력이면 반려견 등록에 성공한다")
    @Test
    void register_validRequest_returnsPetRegistrationResponse() throws Exception {
        PetRegistrationRequest request = validRequest();
        given(petService.register(any(), any())).willReturn(response());

        mockMvc.perform(
                        post("/api/v1/pets")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("초코"))
                .andExpect(jsonPath("$.data.breed.name").value("말티즈"))
                .andExpect(jsonPath("$.data.membershipRole").value("OWNER"));
    }

    @DisplayName("필수 입력값이 누락되면 반려견 등록에 실패한다")
    @Test
    void register_missingRequiredField_returnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/pets")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"초코\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("미래 생년월일이면 반려견 등록에 실패한다")
    @Test
    void register_futureBirthDate_returnsBadRequest() throws Exception {
        PetRegistrationRequest request =
                new PetRegistrationRequest(
                        "초코",
                        LocalDate.now().plusDays(1),
                        Gender.FEMALE,
                        1L,
                        new BigDecimal("4.5"),
                        NeuteredStatus.NEUTERED);

        mockMvc.perform(
                        post("/api/v1/pets")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("체중 경계값이면 반려견 등록에 성공한다")
    @ParameterizedTest
    @ValueSource(strings = {"0.1", "999.9"})
    void register_weightBoundary_returnsPetRegistrationResponse(String weight) throws Exception {
        PetRegistrationRequest request = registrationRequest(new BigDecimal(weight));
        given(petService.register(any(), any())).willReturn(response());

        mockMvc.perform(
                        post("/api/v1/pets")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @DisplayName("체중 범위 또는 소수점 자릿수가 유효하지 않으면 반려견 등록에 실패한다")
    @ParameterizedTest
    @ValueSource(strings = {"0", "1000.0", "4.55"})
    void register_invalidWeight_returnsBadRequest(String weight) throws Exception {
        PetRegistrationRequest request = registrationRequest(new BigDecimal(weight));

        mockMvc.perform(
                        post("/api/v1/pets")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("정상 입력이면 반려견 체중 기록에 성공한다")
    @Test
    void recordWeight_validRequest_returnsPetWeightRecordResponse() throws Exception {
        PetWeightRecordRequest request =
                new PetWeightRecordRequest(
                        new BigDecimal("4.0"), LocalDateTime.of(2024, 1, 1, 10, 30));
        given(petService.recordWeight(any(), any(), any()))
                .willReturn(
                        new PetWeightRecordResponse(
                                10L,
                                new BigDecimal("4.0"),
                                request.measuredAt(),
                                LocalDateTime.of(2026, 7, 25, 10, 30)));

        mockMvc.perform(
                        post("/api/v1/pets/{petId}/weights", 1L)
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.weight").value(4.0))
                .andExpect(jsonPath("$.data.measuredAt").value("2024-01-01T10:30:00"))
                .andExpect(jsonPath("$.data.createdAt").value("2026-07-25T10:30:00"));
    }

    @DisplayName("체중 또는 측정 시각 입력이 유효하지 않으면 체중 기록에 실패한다")
    @Test
    void recordWeight_invalidRequest_returnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/pets/{petId}/weights", 1L)
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"weight\":4.55}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @DisplayName("측정 시각 형식이 올바르지 않으면 체중 기록에 실패한다")
    @Test
    void recordWeight_invalidMeasuredAtFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/pets/{petId}/weights", 1L)
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"weight\":4.0,\"measuredAt\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MESSAGE_NOT_READABLE"));
    }

    private PetRegistrationRequest validRequest() {
        return registrationRequest(new BigDecimal("4.5"));
    }

    private PetRegistrationRequest registrationRequest(BigDecimal weight) {
        return new PetRegistrationRequest(
                "초코", LocalDate.of(2023, 1, 1), Gender.FEMALE, 1L, weight, NeuteredStatus.NEUTERED);
    }

    private PetRegistrationResponse response() {
        return new PetRegistrationResponse(
                1L,
                "초코",
                LocalDate.of(2023, 1, 1),
                Gender.FEMALE,
                new BreedResponse(1L, "말티즈"),
                new BigDecimal("4.5"),
                NeuteredStatus.NEUTERED,
                PetMembershipRole.OWNER);
    }

    private Authentication authenticatedMember() {
        AuthPrincipal principal = AuthPrincipal.authenticated(1L, List.of("ROLE_MEMBER"));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
