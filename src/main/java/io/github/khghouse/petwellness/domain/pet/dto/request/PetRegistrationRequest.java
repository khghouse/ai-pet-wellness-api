package io.github.khghouse.petwellness.domain.pet.dto.request;

import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PetRegistrationRequest(
        @NotBlank String name,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotNull Gender gender,
        @NotNull Long breedId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 9, fraction = 1)
                BigDecimal weight,
        @NotNull NeuteredStatus neuteredStatus) {}
