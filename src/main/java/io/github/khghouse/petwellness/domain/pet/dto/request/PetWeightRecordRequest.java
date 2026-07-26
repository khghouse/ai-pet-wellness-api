package io.github.khghouse.petwellness.domain.pet.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetWeightRecordRequest(
        @NotNull
                @DecimalMin(value = "0.1")
                @DecimalMax(value = "999.9")
                @Digits(integer = 3, fraction = 1)
                BigDecimal weight,
        @NotNull LocalDateTime measuredAt) {}
