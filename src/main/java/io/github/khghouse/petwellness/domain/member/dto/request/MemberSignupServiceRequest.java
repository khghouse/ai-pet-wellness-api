package io.github.khghouse.petwellness.domain.member.dto.request;

public record MemberSignupServiceRequest(String email, String password) {

    public static MemberSignupServiceRequest from(MemberSignupRequest request) {
        return new MemberSignupServiceRequest(request.email(), request.password());
    }
}
