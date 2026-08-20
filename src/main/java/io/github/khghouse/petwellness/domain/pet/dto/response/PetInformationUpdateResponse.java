package io.github.khghouse.petwellness.domain.pet.dto.response;

import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PetInformationUpdateResponse(
        Long id,
        String name,
        LocalDate birthDate,
        Gender gender,
        BreedResponse breed,
        NeuteredStatus neuteredStatus,
        LocalDateTime updatedAt) {

    public static PetInformationUpdateResponse from(Pet pet) {
        return new PetInformationUpdateResponse(
                pet.getId(),
                pet.getName(),
                pet.getBirthDate(),
                pet.getGender(),
                BreedResponse.from(pet.getBreed()),
                pet.getNeuteredStatus(),
                pet.getUpdatedAt());
    }
}
