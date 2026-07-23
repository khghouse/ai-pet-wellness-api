package io.github.khghouse.petwellness.domain.pet.repository;

import io.github.khghouse.petwellness.domain.pet.entity.PetWeight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetWeightRepository extends JpaRepository<PetWeight, Long> {}
