package io.github.khghouse.petwellness.domain.pet.dto.request;

import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import java.time.LocalDate;

public record PetInformationUpdateServiceRequest(
        String name,
        LocalDate birthDate,
        Gender gender,
        Long breedId,
        NeuteredStatus neuteredStatus) {

    public static PetInformationUpdateServiceRequest from(PetInformationUpdateRequest request) {
        return new PetInformationUpdateServiceRequest(
                request.name(),
                request.birthDate(),
                request.gender(),
                request.breedId(),
                request.neuteredStatus());
    }
}
