package io.github.khghouse.petwellness.domain.pet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import io.github.khghouse.petwellness.domain.pet.dto.request.PetRegistrationServiceRequest;
import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
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
}
