package io.github.khghouse.petwellness.domain.pet.dto.request;

import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record PetInformationUpdateRequest(
        @NotBlank String name,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotNull Gender gender,
        @NotNull Long breedId,
        @NotNull NeuteredStatus neuteredStatus) {}
