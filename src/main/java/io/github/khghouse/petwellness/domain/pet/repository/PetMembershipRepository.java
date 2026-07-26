package io.github.khghouse.petwellness.domain.pet.repository;

import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetMembershipRepository
        extends JpaRepository<PetMembership, Long>, PetMembershipRepositoryCustom {

    boolean existsByMemberIdAndPetIdAndRoleInAndStatus(
            Long memberId,
            Long petId,
            Collection<PetMembershipRole> roles,
            PetMembershipStatus status);
}
