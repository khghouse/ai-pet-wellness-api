package io.github.khghouse.petwellness.domain.pet.dto.response;

import io.github.khghouse.petwellness.domain.pet.entity.PetWeight;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetWeightRecordResponse(
        Long id, BigDecimal weight, LocalDateTime measuredAt, LocalDateTime createdAt) {

    public static PetWeightRecordResponse from(PetWeight petWeight) {
        return new PetWeightRecordResponse(
                petWeight.getId(),
                petWeight.getWeight(),
                petWeight.getMeasuredAt(),
                petWeight.getCreatedAt());
    }
}
