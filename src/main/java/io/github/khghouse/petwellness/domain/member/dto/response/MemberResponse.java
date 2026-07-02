package io.github.khghouse.petwellness.domain.member.dto.response;

import io.github.khghouse.petwellness.domain.member.entity.Member;
import io.github.khghouse.petwellness.domain.member.entity.MemberStatus;

public record MemberResponse(Long id, String email, MemberStatus status) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getStatus());
    }
}
