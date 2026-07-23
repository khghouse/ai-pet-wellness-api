package io.github.khghouse.petwellness.domain.pet.dto.response;

import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PetRegistrationResponse(
        Long id,
        String name,
        LocalDate birthDate,
        Gender gender,
        BreedResponse breed,
        BigDecimal weight,
        NeuteredStatus neuteredStatus,
        PetMembershipRole membershipRole) {

    public static PetRegistrationResponse from(Pet pet, BigDecimal weight) {
        return new PetRegistrationResponse(
                pet.getId(),
                pet.getName(),
                pet.getBirthDate(),
                pet.getGender(),
                BreedResponse.from(pet.getBreed()),
                weight,
                pet.getNeuteredStatus(),
                PetMembershipRole.OWNER);
    }
}
