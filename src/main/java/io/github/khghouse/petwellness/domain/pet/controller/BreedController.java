package io.github.khghouse.petwellness.domain.pet.controller;

import io.github.khghouse.common.web.global.response.ApiResponse;
import io.github.khghouse.petwellness.domain.pet.dto.response.BreedResponse;
import io.github.khghouse.petwellness.domain.pet.service.PetService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/breeds")
@RequiredArgsConstructor
public class BreedController {

    private final PetService petService;

    @GetMapping
    public ApiResponse<List<BreedResponse>> getActiveBreeds() {
        return ApiResponse.<List<BreedResponse>>ok(petService.getActiveBreeds());
    }
}
