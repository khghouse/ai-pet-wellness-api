package io.github.khghouse.petwellness.domain.pet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
import io.github.khghouse.petwellness.support.RestDocsSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class PetControllerDocsTest extends RestDocsSupport {

    private final PetService petService = Mockito.mock(PetService.class);

    @Override
    protected Object initController() {
        return new PetController(petService);
    }

    @DisplayName("내 반려견 목록 조회 API를 문서화한다")
    @Test
    void getMyPets_authenticatedMember_generatesRestDocs() throws Exception {
        given(petService.getMyPets(any()))
                .willReturn(
                        List.of(
                                new MyPetResponse(
                                        1L,
                                        "초코",
                                        LocalDate.of(2023, 1, 1),
                                        PetMembershipRole.OWNER)));

        mockMvc.perform(
                        get("/api/v1/pets")
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
                                                .description("요청 성공 여부"),
                                        fieldWithPath("data").type(ARRAY).description("반려견 목록"),
                                        fieldWithPath("data[].id")
                                                .type(NUMBER)
                                                .description("반려견 식별자"),
                                        fieldWithPath("data[].name")
                                                .type(STRING)
                                                .description("반려견 이름"),
                                        fieldWithPath("data[].birthDate")
                                                .type(STRING)
                                                .description("생년월일 (yyyy-MM-dd)"),
                                        fieldWithPath("data[].membershipRole")
                                                .type(STRING)
                                                .description("현재 회원의 멤버십 역할"))));
    }

    @DisplayName("반려견 등록 API를 문서화한다")
    @Test
    void register_validRequest_generatesRestDocs() throws Exception {
        PetRegistrationRequest request =
                new PetRegistrationRequest(
                        "초코",
                        LocalDate.of(2023, 1, 1),
                        Gender.FEMALE,
                        1L,
                        new BigDecimal("4.5"),
                        NeuteredStatus.NEUTERED);
        given(petService.register(any(), any()))
                .willReturn(
                        new PetRegistrationResponse(
                                1L,
                                "초코",
                                LocalDate.of(2023, 1, 1),
                                Gender.FEMALE,
                                new BreedResponse(1L, "말티즈"),
                                new BigDecimal("4.5"),
                                NeuteredStatus.NEUTERED,
                                PetMembershipRole.OWNER));

        mockMvc.perform(
                        post("/api/v1/pets")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION)
                                                .description("Bearer Access Token")),
                                requestFields(
                                        fieldWithPath("name").type(STRING).description("반려견 이름"),
                                        fieldWithPath("birthDate")
                                                .type(STRING)
                                                .description("생년월일 (yyyy-MM-dd)"),
                                        fieldWithPath("gender")
                                                .type(STRING)
                                                .description("성별: MALE, FEMALE, UNKNOWN"),
                                        fieldWithPath("breedId").type(NUMBER).description("견종 식별자"),
                                        fieldWithPath("weight")
                                                .type(NUMBER)
                                                .description("체중(kg), 소수점 한 자리"),
                                        fieldWithPath("neuteredStatus")
                                                .type(STRING)
                                                .description(
                                                        "중성화 상태: NEUTERED, NOT_NEUTERED, UNKNOWN")),
                                responseFields(
                                        fieldWithPath("status")
                                                .type(NUMBER)
                                                .description("HTTP 상태 코드"),
                                        fieldWithPath("success")
                                                .type(BOOLEAN)
                                                .description("요청 성공 여부"),
                                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.id")
                                                .type(NUMBER)
                                                .description("반려견 식별자"),
                                        fieldWithPath("data.name")
                                                .type(STRING)
                                                .description("반려견 이름"),
                                        fieldWithPath("data.birthDate")
                                                .type(STRING)
                                                .description("생년월일"),
                                        fieldWithPath("data.gender").type(STRING).description("성별"),
                                        fieldWithPath("data.breed")
                                                .type(OBJECT)
                                                .description("견종 정보"),
                                        fieldWithPath("data.breed.id")
                                                .type(NUMBER)
                                                .description("견종 식별자"),
                                        fieldWithPath("data.breed.name")
                                                .type(STRING)
                                                .description("견종명"),
                                        fieldWithPath("data.weight")
                                                .type(NUMBER)
                                                .description("등록 체중(kg)"),
                                        fieldWithPath("data.neuteredStatus")
                                                .type(STRING)
                                                .description("중성화 상태"),
                                        fieldWithPath("data.membershipRole")
                                                .type(STRING)
                                                .description("생성된 멤버십 역할"))));
    }

    @DisplayName("반려견 체중 기록 API를 문서화한다")
    @Test
    void recordWeight_validRequest_generatesRestDocs() throws Exception {
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
                                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                                .principal(authenticatedMember())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION)
                                                .description("Bearer Access Token")),
                                pathParameters(parameterWithName("petId").description("반려견 식별자")),
                                requestFields(
                                        fieldWithPath("weight")
                                                .type(NUMBER)
                                                .description("체중(kg), 0.1~999.9, 소수점 한 자리"),
                                        fieldWithPath("measuredAt")
                                                .type(STRING)
                                                .description("실제 측정 시각 (ISO-8601)")),
                                responseFields(
                                        fieldWithPath("status")
                                                .type(NUMBER)
                                                .description("HTTP 상태 코드"),
                                        fieldWithPath("success")
                                                .type(BOOLEAN)
                                                .description("요청 성공 여부"),
                                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                                        fieldWithPath("data.id")
                                                .type(NUMBER)
                                                .description("체중 이력 식별자"),
                                        fieldWithPath("data.weight")
                                                .type(NUMBER)
                                                .description("기록 체중(kg)"),
                                        fieldWithPath("data.measuredAt")
                                                .type(STRING)
                                                .description("실제 측정 시각"),
                                        fieldWithPath("data.createdAt")
                                                .type(STRING)
                                                .description("체중 이력 등록 시각"))));
    }

    private Authentication authenticatedMember() {
        AuthPrincipal principal = AuthPrincipal.authenticated(1L, List.of("ROLE_MEMBER"));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
