package io.github.khghouse.petwellness.domain.member.controller;

import io.github.khghouse.common.auth.global.security.AuthPrincipal;
import io.github.khghouse.common.web.global.response.ApiResponse;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.dto.response.MemberResponse;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ApiResponse<MemberResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        MemberResponse response = memberService.signup(MemberSignupServiceRequest.from(request));
        return ApiResponse.<MemberResponse>ok(response);
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMe(Authentication authentication) {
        MemberResponse response = memberService.getMember(getAuthenticatedMemberId(authentication));
        return ApiResponse.<MemberResponse>ok(response);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(Authentication authentication) {
        memberService.withdraw(getAuthenticatedMemberId(authentication));
        return ApiResponse.<Void>ok();
    }

    private Long getAuthenticatedMemberId(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        return principal.getUserId();
    }
}
