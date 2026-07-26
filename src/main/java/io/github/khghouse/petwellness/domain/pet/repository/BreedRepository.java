package io.github.khghouse.petwellness.domain.pet.repository;

import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedRepository extends JpaRepository<Breed, Long> {

    List<Breed> findAllByActiveTrueOrderByNameAsc();
}
