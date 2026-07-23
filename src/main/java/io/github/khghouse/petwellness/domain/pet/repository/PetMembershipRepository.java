package io.github.khghouse.petwellness.domain.pet.repository;

import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetMembershipRepository extends JpaRepository<PetMembership, Long> {}
