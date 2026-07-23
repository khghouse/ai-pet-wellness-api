package io.github.khghouse.petwellness.domain.pet.repository;

import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {}
