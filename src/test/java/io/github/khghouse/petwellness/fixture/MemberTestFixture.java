package io.github.khghouse.petwellness.fixture;

import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.repository.MemberRepository;

public final class MemberTestFixture {

    private MemberTestFixture() {}

    public static void withdraw(MemberRepository memberRepository, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.withdraw();
        memberRepository.saveAndFlush(member);
    }
}
