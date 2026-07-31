package io.github.khghouse.petwellness.domain.pet.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;
import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PetMembershipRepositoryTest extends RepositoryTestSupport {

    @Autowired private PetMembershipRepository petMembershipRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BreedRepository breedRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private EntityManager entityManager;

    @DisplayName("활성 멤버십의 삭제되지 않은 반려견을 fetch join으로 최근 등록순 조회한다")
    @Test
    void findActiveMembershipsWithPetByMemberId_returnsFetchedPetsInRecentRegistrationOrder() {
        Member member =
                memberRepository.save(Member.create("member@example.com", "encoded-password"));
        Breed breed = breedRepository.save(Breed.create("테스트 견종", true));
        Pet oldestPet = petRepository.save(createPet("첫째", breed));
        Pet newestPet = petRepository.save(createPet("둘째", breed));
        petMembershipRepository.save(PetMembership.createOwner(member, oldestPet));
        petMembershipRepository.save(PetMembership.createOwner(member, newestPet));
        entityManager.flush();
        updateCreatedAt(oldestPet.getId(), LocalDateTime.of(2026, 7, 1, 10, 0));
        updateCreatedAt(newestPet.getId(), LocalDateTime.of(2026, 7, 2, 10, 0));
        entityManager.clear();

        var memberships =
                petMembershipRepository.findActiveMembershipsWithPetByMemberId(member.getId());

        assertThat(memberships)
                .extracting(membership -> membership.getPet().getName())
                .containsExactly("둘째", "첫째");
        assertThat(memberships)
                .allSatisfy(
                        membership ->
                                assertThat(Hibernate.isInitialized(membership.getPet())).isTrue());
        assertThat(memberships)
                .allSatisfy(
                        membership ->
                                assertThat(Hibernate.isInitialized(membership.getMember()))
                                        .isFalse());
    }

    private Pet createPet(String name, Breed breed) {
        return Pet.create(
                name, LocalDate.of(2023, 1, 1), Gender.FEMALE, breed, NeuteredStatus.NEUTERED);
    }

    private void updateCreatedAt(Long petId, LocalDateTime createdAt) {
        entityManager
                .createNativeQuery("update pet set created_at = :createdAt where id = :petId")
                .setParameter("createdAt", createdAt)
                .setParameter("petId", petId)
                .executeUpdate();
    }
}
