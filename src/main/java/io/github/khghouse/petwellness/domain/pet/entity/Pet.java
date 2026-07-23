package io.github.khghouse.petwellness.domain.pet.entity;

import io.github.khghouse.petwellness.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id", nullable = false)
    private Breed breed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NeuteredStatus neuteredStatus;

    @Column(nullable = false)
    private boolean deleted;

    private Pet(
            String name,
            LocalDate birthDate,
            Gender gender,
            Breed breed,
            NeuteredStatus neuteredStatus) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.breed = breed;
        this.neuteredStatus = neuteredStatus;
        this.deleted = false;
    }

    public static Pet create(
            String name,
            LocalDate birthDate,
            Gender gender,
            Breed breed,
            NeuteredStatus neuteredStatus) {
        return new Pet(name, birthDate, gender, breed, neuteredStatus);
    }
}
