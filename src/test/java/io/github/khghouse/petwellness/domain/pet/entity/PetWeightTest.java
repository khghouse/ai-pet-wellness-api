package io.github.khghouse.petwellness.domain.pet.entity;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PetWeightTest {

    @DisplayName("체중 경계값이면 체중 이력을 생성한다")
    @ParameterizedTest
    @ValueSource(strings = {"0.1", "999.9"})
    void create_weightBoundary_createsPetWeight(String weight) {
        assertThatCode(() -> PetWeight.create(createPet(), new BigDecimal(weight), measuredAt()))
                .doesNotThrowAnyException();
    }

    @DisplayName("체중 범위 또는 소수점 자릿수가 유효하지 않으면 체중 이력을 생성할 수 없다")
    @ParameterizedTest
    @ValueSource(strings = {"0", "1000.0", "4.55"})
    void create_invalidWeight_throwsIllegalArgumentException(String weight) {
        assertThatThrownBy(
                        () -> PetWeight.create(createPet(), new BigDecimal(weight), measuredAt()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Pet createPet() {
        return Pet.create(
                "초코",
                LocalDate.of(2023, 1, 1),
                Gender.FEMALE,
                Breed.create("테스트 견종", true),
                NeuteredStatus.NEUTERED);
    }

    private LocalDateTime measuredAt() {
        return LocalDateTime.of(2026, 8, 1, 10, 0);
    }
}
