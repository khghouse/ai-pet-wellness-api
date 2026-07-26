package io.github.khghouse.petwellness.domain.pet.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.petwellness.domain.pet.dto.response.BreedResponse;
import io.github.khghouse.petwellness.domain.pet.service.PetService;
import io.github.khghouse.petwellness.support.RestDocsSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BreedControllerDocsTest extends RestDocsSupport {

    private final PetService petService = Mockito.mock(PetService.class);

    @Override
    protected Object initController() {
        return new BreedController(petService);
    }

    @DisplayName("활성 견종 목록 조회 API를 문서화한다")
    @Test
    void getActiveBreeds_generatesRestDocs() throws Exception {
        given(petService.getActiveBreeds())
                .willReturn(List.of(new BreedResponse(1L, "말티즈"), new BreedResponse(2L, "푸들")));

        mockMvc.perform(get("/api/v1/breeds"))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{class-name}/{method-name}",
                                responseFields(
                                        fieldWithPath("status")
                                                .type(NUMBER)
                                                .description("HTTP 상태 코드"),
                                        fieldWithPath("success")
                                                .type(BOOLEAN)
                                                .description("요청 성공 여부"),
                                        fieldWithPath("data").type(ARRAY).description("활성 견종 목록"),
                                        fieldWithPath("data[].id")
                                                .type(NUMBER)
                                                .description("견종 식별자"),
                                        fieldWithPath("data[].name")
                                                .type(STRING)
                                                .description("견종명"))));
    }
}
