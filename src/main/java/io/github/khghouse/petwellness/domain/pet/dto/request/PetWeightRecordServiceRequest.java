package io.github.khghouse.petwellness.domain.pet.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PetWeightRecordServiceRequest(BigDecimal weight, LocalDateTime measuredAt) {

    public static PetWeightRecordServiceRequest from(PetWeightRecordRequest request) {
        return new PetWeightRecordServiceRequest(request.weight(), request.measuredAt());
    }
}
