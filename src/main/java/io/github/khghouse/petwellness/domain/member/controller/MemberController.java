package io.github.khghouse.petwellness.domain.member.controller;

import io.github.khghouse.common.web.global.response.ApiResponse;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberLoginRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberLoginServiceRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupRequest;
import io.github.khghouse.petwellness.domain.member.dto.request.MemberSignupServiceRequest;
import io.github.khghouse.petwellness.domain.member.dto.response.MemberResponse;
import io.github.khghouse.petwellness.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/login")
    public ApiResponse<MemberResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        MemberResponse response = memberService.login(MemberLoginServiceRequest.from(request));
        return ApiResponse.<MemberResponse>ok(response);
    }
}
