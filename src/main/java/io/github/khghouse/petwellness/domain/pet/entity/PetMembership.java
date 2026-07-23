package io.github.khghouse.petwellness.domain.pet.entity;

import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.global.entity.BaseEntity;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_pet_membership_member_pet",
                        columnNames = {"member_id", "pet_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private Member member;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    private PetMembershipRole role;

    @Enumerated(EnumType.STRING)
    private PetMembershipStatus status;

    private PetMembership(Member member, Pet pet) {
        this.member = member;
        this.pet = pet;
        this.role = PetMembershipRole.OWNER;
        this.status = PetMembershipStatus.ACTIVE;
    }

    public static PetMembership createOwner(Member member, Pet pet) {
        return new PetMembership(member, pet);
    }
}
