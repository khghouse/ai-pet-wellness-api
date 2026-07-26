package io.github.khghouse.petwellness.domain.pet.repository;

import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipRole;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PetMembershipRepository extends JpaRepository<PetMembership, Long> {

    boolean existsByMemberIdAndPetIdAndRoleInAndStatus(
            Long memberId,
            Long petId,
            Collection<PetMembershipRole> roles,
            PetMembershipStatus status);

    @Query(
            """
            select petMembership
            from PetMembership petMembership
            join fetch petMembership.pet pet
            where petMembership.member.id = :memberId
                and petMembership.status = :status
                and pet.deleted = false
            order by pet.createdAt desc, pet.id desc
            """)
    List<PetMembership> findActiveMembershipsWithPetByMemberId(
            @Param("memberId") Long memberId, @Param("status") PetMembershipStatus status);
}
