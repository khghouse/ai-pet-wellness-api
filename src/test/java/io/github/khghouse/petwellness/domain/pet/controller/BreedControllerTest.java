package io.github.khghouse.petwellness.domain.pet.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.khghouse.petwellness.domain.pet.dto.response.BreedResponse;
import io.github.khghouse.petwellness.domain.pet.service.PetService;
import io.github.khghouse.petwellness.support.ControllerTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(BreedController.class)
class BreedControllerTest extends ControllerTestSupport {

    @MockitoBean private PetService petService;

    @DisplayName("활성 견종 목록을 필요한 필드만 반환한다")
    @Test
    void getActiveBreeds_returnsBreedResponses() throws Exception {
        given(petService.getActiveBreeds())
                .willReturn(List.of(new BreedResponse(1L, "말티즈"), new BreedResponse(2L, "푸들")));

        mockMvc.perform(get("/api/v1/breeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("말티즈"))
                .andExpect(jsonPath("$.data[0].active").doesNotExist())
                .andExpect(jsonPath("$.data[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.data[0].updatedAt").doesNotExist());
    }

    @DisplayName("활성 견종이 없으면 빈 배열을 반환한다")
    @Test
    void getActiveBreeds_noActiveBreed_returnsEmptyArray() throws Exception {
        given(petService.getActiveBreeds()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/breeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
