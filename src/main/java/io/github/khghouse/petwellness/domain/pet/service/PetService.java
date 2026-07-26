package io.github.khghouse.petwellness.domain.pet.service;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationServiceRequest;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetWeightRecordServiceRequest;
import io.github.khghouse.petwellness.domain.pet.dto.response.MyPetResponse;
import io.github.khghouse.petwellness.domain.pet.dto.response.PetRegistrationResponse;
import io.github.khghouse.petwellness.domain.pet.dto.response.PetWeightRecordResponse;
import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipStatus;
import io.github.khghouse.petwellness.domain.pet.entity.PetWeight;
import io.github.khghouse.petwellness.domain.pet.exception.PetErrorCode;
import io.github.khghouse.petwellness.domain.pet.repository.BreedRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetMembershipRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetWeightRepository;
import java.time.LocalDateTime;
import java.util.List;
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

        petWeightRepository.save(
                PetWeight.create(pet, request.weight(), registeredAt, registeredAt));
        petMembershipRepository.save(PetMembership.createOwner(member, pet));

        return PetRegistrationResponse.from(pet, request.weight());
    }

    @Transactional
    public PetWeightRecordResponse recordWeight(
            Long memberId, Long petId, PetWeightRecordServiceRequest request) {
        Pet pet = getActivePet(petId);
        validateWeightRecordPermission(memberId, petId);
        validateMeasuredAt(pet, request.measuredAt());

        PetWeight petWeight =
                petWeightRepository.save(
                        PetWeight.create(
                                pet, request.weight(), request.measuredAt(), LocalDateTime.now()));
        return PetWeightRecordResponse.from(petWeight);
    }

    @Transactional(readOnly = true)
    public List<MyPetResponse> getMyPets(Long memberId) {
        return petMembershipRepository
                .findActiveMembershipsWithPetByMemberId(memberId, PetMembershipStatus.ACTIVE)
                .stream()
                .map(MyPetResponse::from)
                .toList();
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

    private Pet getActivePet(Long petId) {
        return petRepository
                .findByIdAndDeletedFalse(petId)
                .orElseThrow(() -> new CustomException(PetErrorCode.PET_NOT_FOUND));
    }

    private void validateWeightRecordPermission(Long memberId, Long petId) {
        boolean hasPermission =
                petMembershipRepository.existsByMemberIdAndPetIdAndRoleInAndStatus(
                        memberId,
                        petId,
                        List.of(PetMembershipRole.OWNER, PetMembershipRole.FAMILY),
                        PetMembershipStatus.ACTIVE);
        if (!hasPermission) {
            throw new CustomException(PetErrorCode.PET_MEMBERSHIP_FORBIDDEN);
        }
    }

    private void validateMeasuredAt(Pet pet, LocalDateTime measuredAt) {
        if (measuredAt.isBefore(pet.getBirthDate().atStartOfDay())) {
            throw new CustomException(PetErrorCode.WEIGHT_MEASURED_AT_BEFORE_BIRTH_DATE);
        }
        if (measuredAt.isAfter(LocalDateTime.now())) {
            throw new CustomException(PetErrorCode.WEIGHT_MEASURED_AT_IN_FUTURE);
        }
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
