package io.github.khghouse.petwellness.domain.pet.dto.request;

import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PetRegistrationServiceRequest(
        String name,
        LocalDate birthDate,
        Gender gender,
        Long breedId,
        BigDecimal weight,
        NeuteredStatus neuteredStatus) {

    public static PetRegistrationServiceRequest from(PetRegistrationRequest request) {
        return new PetRegistrationServiceRequest(
                request.name(),
                request.birthDate(),
                request.gender(),
                request.breedId(),
                request.weight(),
                request.neuteredStatus());
    }
}
