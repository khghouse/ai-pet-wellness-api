package io.github.khghouse.petwellness;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.khghouse.petwellness.domain.pet.entity.Breed;
import io.github.khghouse.petwellness.domain.pet.entity.Gender;
import io.github.khghouse.petwellness.domain.pet.entity.NeuteredStatus;
import io.github.khghouse.petwellness.domain.pet.entity.Pet;
import io.github.khghouse.petwellness.domain.pet.entity.PetWeight;
import io.github.khghouse.petwellness.domain.pet.repository.BreedRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetRepository;
import io.github.khghouse.petwellness.domain.pet.repository.PetWeightRepository;
import io.github.khghouse.petwellness.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class H2TimeZoneConfigurationTest extends RepositoryTestSupport {

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private BreedRepository breedRepository;

    @Autowired private PetRepository petRepository;

    @Autowired private PetWeightRepository petWeightRepository;

    @Autowired private EntityManager entityManager;

    @DisplayName("H2 세션 시간대는 한국 표준시 오프셋이다")
    @Test
    void currentTimestamp_hasKoreanStandardTimeOffset() {
        OffsetDateTime currentTimestamp =
                jdbcTemplate.queryForObject(
                        "select current_timestamp",
                        (resultSet, rowNum) -> resultSet.getObject(1, OffsetDateTime.class));

        assertThat(currentTimestamp.getOffset()).isEqualTo(ZoneOffset.ofHours(9));
    }

    @DisplayName("H2는 LocalDateTime을 시간 변환 없이 저장하고 조회한다")
    @Test
    void petWeight_measuredAt_isPersistedWithoutTimeConversion() {
        Breed breed = breedRepository.save(Breed.create("테스트 견종", true));
        Pet pet =
                petRepository.save(
                        Pet.create(
                                "테스트 반려견",
                                LocalDate.of(2020, 1, 1),
                                Gender.MALE,
                                breed,
                                NeuteredStatus.NEUTERED));
        LocalDateTime measuredAt = LocalDateTime.of(2026, 7, 25, 14, 30, 0, 123_456_000);

        PetWeight petWeight =
                petWeightRepository.saveAndFlush(
                        PetWeight.create(pet, new BigDecimal("4.8"), measuredAt));
        entityManager.clear();

        PetWeight persistedPetWeight =
                petWeightRepository.findById(petWeight.getId()).orElseThrow();

        assertThat(persistedPetWeight.getMeasuredAt()).isEqualTo(measuredAt);
    }
}
