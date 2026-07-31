package io.github.khghouse.petwellness.domain.pet.dto.request;

import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import jakarta.validation.constraints.DecimalMax;
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
        @NotNull
                @DecimalMin(value = "0.1")
                @DecimalMax(value = "999.9")
                @Digits(integer = 3, fraction = 1)
                BigDecimal weight,
        @NotNull NeuteredStatus neuteredStatus) {}
