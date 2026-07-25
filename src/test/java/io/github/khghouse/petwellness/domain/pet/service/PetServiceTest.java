package io.github.khghouse.petwellness.domain.pet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationServiceRequest;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetWeightRecordServiceRequest;
import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipStatus;
import io.github.khghouse.petwellness.domain.pet.exception.PetErrorCode;
import io.github.khghouse.petwellness.domain.pet.repository.BreedRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetMembershipRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetWeightRepository;
import io.github.khghouse.petwellness.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class PetServiceTest extends IntegrationTestSupport {

    @Autowired private PetService petService;
    @Autowired private MemberService memberService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BreedRepository breedRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private PetWeightRepository petWeightRepository;
    @Autowired private PetMembershipRepository petMembershipRepository;

    @DisplayName("정상 요청이면 반려견, 첫 체중 이력과 소유자 멤버십을 생성한다")
    @Test
    void register_validRequest_persistsPetWeightAndOwnerMembership() {
        Member member = createMember();
        Breed breed = breedRepository.save(Breed.create("테스트 견종", true));
        PetRegistrationServiceRequest request = registrationRequest(breed.getId());

        var response = petService.register(member.getId(), request);

        assertThat(response.id()).isNotNull();
        assertThat(response.breed().name()).isEqualTo("테스트 견종");
        assertThat(response.membershipRole()).isEqualTo(PetMembershipRole.OWNER);
        assertThat(petRepository.count()).isEqualTo(1);
        assertThat(petWeightRepository.count()).isEqualTo(1);
        assertThat(petMembershipRepository.count()).isEqualTo(1);

        var weight = petWeightRepository.findAll().get(0);
        assertThat(weight.getWeight()).isEqualByComparingTo("4.5");
        assertThat(weight.getMeasuredAt()).isEqualTo(weight.getCreatedAt());

        var membership = petMembershipRepository.findAll().get(0);
        assertThat(membership.getMember().getId()).isEqualTo(member.getId());
        assertThat(membership.getRole()).isEqualTo(PetMembershipRole.OWNER);
        assertThat(membership.getStatus()).isEqualTo(PetMembershipStatus.ACTIVE);
    }

    @DisplayName("존재하지 않는 견종이면 반려견, 체중 이력과 멤버십을 생성하지 않는다")
    @Test
    void register_missingBreed_doesNotPersistRegistrationData() {
        Member member = createMember();

        assertThatThrownBy(() -> petService.register(member.getId(), registrationRequest(999L)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PetErrorCode.BREED_NOT_FOUND);

        assertThat(petRepository.count()).isZero();
        assertThat(petWeightRepository.count()).isZero();
        assertThat(petMembershipRepository.count()).isZero();
    }

    @DisplayName("비활성 견종이면 반려견 등록에 실패한다")
    @Test
    void register_inactiveBreed_throwsBreedInactive() {
        Member member = createMember();
        Breed breed = breedRepository.save(Breed.create("비활성 테스트 견종", false));

        assertThatThrownBy(
                        () ->
                                petService.register(
                                        member.getId(), registrationRequest(breed.getId())))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PetErrorCode.BREED_INACTIVE);
    }

    @DisplayName("소유자 회원은 정수 체중을 소수점 한 자리 이력으로 기록한다")
    @Test
    void recordWeight_ownerMembership_persistsNormalizedWeight() {
        Member member = createMember();
        Pet pet = createPet();
        petMembershipRepository.save(PetMembership.createOwner(member, pet));
        LocalDateTime measuredAt = LocalDateTime.of(2024, 1, 1, 10, 30);

        var response =
                petService.recordWeight(
                        member.getId(),
                        pet.getId(),
                        new PetWeightRecordServiceRequest(new BigDecimal("4"), measuredAt));

        assertThat(response.id()).isNotNull();
        assertThat(response.weight()).isEqualByComparingTo("4.0");
        assertThat(response.measuredAt()).isEqualTo(measuredAt);
        assertThat(response.createdAt()).isNotNull();
        assertThat(petWeightRepository.count()).isEqualTo(1);
        assertThat(petWeightRepository.findAll().get(0).getWeight()).isEqualByComparingTo("4.0");
    }

    @DisplayName("가족 회원은 체중 이력을 기록한다")
    @Test
    void recordWeight_familyMembership_persistsWeight() {
        Member member = createMember();
        Pet pet = createPet();
        petMembershipRepository.save(
                PetMembership.create(
                        member, pet, PetMembershipRole.FAMILY, PetMembershipStatus.ACTIVE));

        petService.recordWeight(
                member.getId(),
                pet.getId(),
                new PetWeightRecordServiceRequest(
                        new BigDecimal("4.2"), LocalDateTime.of(2024, 1, 1, 10, 30)));

        assertThat(petWeightRepository.count()).isEqualTo(1);
    }

    @DisplayName("같은 측정 시각으로 요청해도 매번 새 체중 이력을 생성한다")
    @Test
    void recordWeight_sameMeasuredAt_persistsSeparateHistories() {
        Member member = createMember();
        Pet pet = createPet();
        petMembershipRepository.save(PetMembership.createOwner(member, pet));
        LocalDateTime measuredAt = LocalDateTime.of(2024, 1, 1, 10, 30);

        var first =
                petService.recordWeight(
                        member.getId(),
                        pet.getId(),
                        new PetWeightRecordServiceRequest(new BigDecimal("4.0"), measuredAt));
        var second =
                petService.recordWeight(
                        member.getId(),
                        pet.getId(),
                        new PetWeightRecordServiceRequest(new BigDecimal("4.1"), measuredAt));

        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(petWeightRepository.count()).isEqualTo(2);
    }

    @DisplayName("LEFT 멤버십 회원은 체중을 기록할 수 없다")
    @Test
    void recordWeight_leftMembership_throwsMembershipForbidden() {
        Member member = createMember();
        Pet pet = createPet();
        petMembershipRepository.save(
                PetMembership.create(
                        member, pet, PetMembershipRole.OWNER, PetMembershipStatus.LEFT));

        assertThatThrownBy(
                        () ->
                                petService.recordWeight(
                                        member.getId(),
                                        pet.getId(),
                                        new PetWeightRecordServiceRequest(
                                                new BigDecimal("4.0"),
                                                LocalDateTime.of(2024, 1, 1, 10, 30))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PetErrorCode.PET_MEMBERSHIP_FORBIDDEN);
    }

    @DisplayName("존재하지 않거나 삭제된 반려견에는 체중을 기록할 수 없다")
    @Test
    void recordWeight_missingOrDeletedPet_throwsPetNotFound() {
        Member member = createMember();
        Pet pet = createPet();
        pet.delete();

        assertThatThrownBy(
                        () ->
                                petService.recordWeight(
                                        member.getId(),
                                        pet.getId(),
                                        new PetWeightRecordServiceRequest(
                                                new BigDecimal("4.0"),
                                                LocalDateTime.of(2024, 1, 1, 10, 30))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PetErrorCode.PET_NOT_FOUND);
    }

    @DisplayName("생년월일 이전 또는 미래 측정 시각이면 체중 기록에 실패한다")
    @Test
    void recordWeight_invalidMeasuredAt_throwsPolicyError() {
        Member member = createMember();
        Pet pet = createPet();
        petMembershipRepository.save(PetMembership.createOwner(member, pet));

        assertThatThrownBy(
                        () ->
                                petService.recordWeight(
                                        member.getId(),
                                        pet.getId(),
                                        new PetWeightRecordServiceRequest(
                                                new BigDecimal("4.0"),
                                                LocalDateTime.of(2022, 12, 31, 23, 59))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PetErrorCode.WEIGHT_MEASURED_AT_BEFORE_BIRTH_DATE);

        assertThatThrownBy(
                        () ->
                                petService.recordWeight(
                                        member.getId(),
                                        pet.getId(),
                                        new PetWeightRecordServiceRequest(
                                                new BigDecimal("4.0"),
                                                LocalDateTime.now().plusMinutes(1))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PetErrorCode.WEIGHT_MEASURED_AT_IN_FUTURE);
    }

    private Member createMember() {
        memberService.signup(new MemberSignupServiceRequest("member@example.com", "password1"));
        return memberRepository.findByEmail("member@example.com").orElseThrow();
    }

    private PetRegistrationServiceRequest registrationRequest(Long breedId) {
        return new PetRegistrationServiceRequest(
                "초코",
                LocalDate.of(2023, 1, 1),
                Gender.FEMALE,
                breedId,
                new BigDecimal("4.5"),
                NeuteredStatus.NEUTERED);
    }

    private Pet createPet() {
        Breed breed = breedRepository.save(Breed.create("체중 테스트 견종", true));
        return petRepository.save(
                Pet.create(
                        "초코",
                        LocalDate.of(2023, 1, 1),
                        Gender.FEMALE,
                        breed,
                        NeuteredStatus.NEUTERED));
    }
}
