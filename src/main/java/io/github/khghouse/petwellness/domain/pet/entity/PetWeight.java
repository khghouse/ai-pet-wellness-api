package io.github.khghouse.petwellness.domain.pet.entity;

import io.github.khghouse.common.core.global.exception.CustomException;
import io.github.khghouse.petwellness.domain.pet.exception.PetErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetWeight {

    private static final BigDecimal MIN_WEIGHT = new BigDecimal("0.1");
    private static final BigDecimal MAX_WEIGHT = new BigDecimal("999.9");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, precision = 10, scale = 1)
    private BigDecimal weight;

    @Column(nullable = false)
    private LocalDateTime measuredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private PetWeight(
            Pet pet, BigDecimal weight, LocalDateTime measuredAt, LocalDateTime createdAt) {
        this.pet = pet;
        this.weight = validateAndNormalizeWeight(weight);
        this.measuredAt = measuredAt;
        this.createdAt = createdAt;
    }

    public static PetWeight create(
            Pet pet, BigDecimal weight, LocalDateTime measuredAt, LocalDateTime createdAt) {
        return new PetWeight(pet, weight, measuredAt, createdAt);
    }

    public static PetWeight create(Pet pet, BigDecimal weight, LocalDateTime measuredAt) {
        return new PetWeight(pet, weight, measuredAt, measuredAt);
    }

    private static BigDecimal validateAndNormalizeWeight(BigDecimal weight) {
        if (weight == null
                || weight.compareTo(MIN_WEIGHT) < 0
                || weight.compareTo(MAX_WEIGHT) > 0
                || weight.scale() > 1) {
            throw new CustomException(PetErrorCode.WEIGHT_INVALID);
        }
        return weight.setScale(1, RoundingMode.UNNECESSARY);
    }
}
