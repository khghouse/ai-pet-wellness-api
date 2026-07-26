package io.github.khghouse.petwellness.domain.pet.dto.response;

import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import java.time.LocalDate;

public record MyPetResponse(
        Long id, String name, LocalDate birthDate, PetMembershipRole membershipRole) {

    public static MyPetResponse from(PetMembership petMembership) {
        return new MyPetResponse(
                petMembership.getPet().getId(),
                petMembership.getPet().getName(),
                petMembership.getPet().getBirthDate(),
                petMembership.getRole());
    }
}
