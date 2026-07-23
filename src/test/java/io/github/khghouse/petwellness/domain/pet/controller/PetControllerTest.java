package io.github.khghouse.petwellness.domain.pet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.common.auth.global.security.AuthPrincipal;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationRequest;
import io.github.khghouse.petwellness.domain.pet.dto.response.BreedResponse;
import io.github.khghouse.petwellness.domain.pet.dto.response.PetRegistrationResponse;
import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import io.github.khghouse.petwellness.domain.pet.service.PetService;
import io.github.khghouse.petwellness.support.ControllerTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(PetController.class)
class PetControllerTest extends ControllerTestSupport {

    @MockitoBean private PetService petService;

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

    @DisplayName("유효하지 않은 체중이면 반려견 등록에 실패한다")
    @Test
    void register_invalidWeight_returnsBadRequest() throws Exception {
        PetRegistrationRequest request =
                new PetRegistrationRequest(
                        "초코",
                        LocalDate.of(2023, 1, 1),
                        Gender.FEMALE,
                        1L,
                        new BigDecimal("4.55"),
                        NeuteredStatus.NEUTERED);

        mockMvc.perform(
                        post("/api/v1/pets")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    private PetRegistrationRequest validRequest() {
        return new PetRegistrationRequest(
                "초코",
                LocalDate.of(2023, 1, 1),
                Gender.FEMALE,
                1L,
                new BigDecimal("4.5"),
                NeuteredStatus.NEUTERED);
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
