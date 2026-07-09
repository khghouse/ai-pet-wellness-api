package io.github.khghouse.petwellness.domain.member.dto.request;

public record MemberLoginServiceRequest(String email, String password) {

    public static MemberLoginServiceRequest from(MemberLoginRequest request) {
        return new MemberLoginServiceRequest(request.email(), request.password());
    }
}
