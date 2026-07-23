package io.github.khghouse.petwellness.domain.pet.controller;

import io.github.khghouse.common.auth.global.security.AuthPrincipal;
import io.github.khghouse.common.web.global.response.ApiResponse;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationRequest;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationServiceRequest;
import io.github.khghouse.petwellness.domain.pet.dto.response.PetRegistrationResponse;
import io.github.khghouse.petwellness.domain.pet.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ApiResponse<PetRegistrationResponse> register(
            Authentication authentication, @Valid @RequestBody PetRegistrationRequest request) {
        PetRegistrationResponse response =
                petService.register(
                        getAuthenticatedMemberId(authentication),
                        PetRegistrationServiceRequest.from(request));
        return ApiResponse.<PetRegistrationResponse>ok(response);
    }

    private Long getAuthenticatedMemberId(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return principal.getUserId();
    }
}
