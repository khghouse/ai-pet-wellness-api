package io.github.khghouse.petwellness.domain.pet.repository;

import static io.github.khghouse.petwellness.domain.pet.entity.QPet.pet;
import static io.github.khghouse.petwellness.domain.pet.entity.QPetMembership.petMembership;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembership;
import io.github.khghouse.petwellness.domain.pet.entity.PetMembershipStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PetMembershipRepositoryCustomImpl implements PetMembershipRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PetMembership> findActiveMembershipsWithPetByMemberId(Long memberId) {
        return queryFactory
                .selectFrom(petMembership)
                .join(petMembership.pet, pet)
                .fetchJoin()
                .where(
                        petMembership.member.id.eq(memberId),
                        petMembership.status.eq(PetMembershipStatus.ACTIVE),
                        pet.deleted.isFalse())
                .orderBy(pet.createdAt.desc(), pet.id.desc())
                .fetch();
    }
}
