package io.github.khghouse.petwellness.domain.pet.repository;

import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import java.util.List;

public interface PetMembershipRepositoryCustom {

    List<PetMembership> findActiveMembershipsWithPetByMemberId(Long memberId);
}
