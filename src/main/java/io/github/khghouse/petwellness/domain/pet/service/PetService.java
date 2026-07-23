package io.github.khghouse.petwellness.domain.pet.service;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationServiceRequest;
import io.github.khghouse.petwellness.domain.pet.dto.response.PetRegistrationResponse;
import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.domain.pet.entity.PetWeight;
import io.github.khghouse.petwellness.domain.pet.exception.PetErrorCode;
import io.github.khghouse.petwellness.domain.pet.repository.BreedRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetMembershipRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetWeightRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetService {

    private final MemberService memberService;
    private final BreedRepository breedRepository;
    private final PetRepository petRepository;
    private final PetWeightRepository petWeightRepository;
    private final PetMembershipRepository petMembershipRepository;

    @Transactional
    public PetRegistrationResponse register(Long memberId, PetRegistrationServiceRequest request) {
        Member member = memberService.getActiveMember(memberId);
        Breed breed = getActiveBreed(request.breedId());
        Pet pet = petRepository.save(createPet(request, breed));
        LocalDateTime registeredAt = LocalDateTime.now();

        petWeightRepository.save(PetWeight.create(pet, request.weight(), registeredAt));
        petMembershipRepository.save(PetMembership.createOwner(member, pet));

        return PetRegistrationResponse.from(pet, request.weight());
    }

    private Breed getActiveBreed(Long breedId) {
        Breed breed =
                breedRepository
                        .findById(breedId)
                        .orElseThrow(() -> new CustomException(PetErrorCode.BREED_NOT_FOUND));

        if (!breed.isActive()) {
            throw new CustomException(PetErrorCode.BREED_INACTIVE);
        }
        return breed;
    }

    private Pet createPet(PetRegistrationServiceRequest request, Breed breed) {
        return Pet.create(
                request.name(),
                request.birthDate(),
                request.gender(),
                breed,
                request.neuteredStatus());
    }
}
