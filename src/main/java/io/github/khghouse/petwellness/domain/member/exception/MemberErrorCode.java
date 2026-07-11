package io.github.khghouse.petwellness.domain.member.exception;

import io.github.khghouse.common.core.global.exception.ErrorInfo;

public enum MemberErrorCode implements ErrorInfo {
    EMAIL_DUPLICATED(409, "EMAIL_DUPLICATED", "이미 가입된 이메일입니다."),
    MEMBER_NOT_FOUND(404, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_WITHDRAWN(422, "MEMBER_ALREADY_WITHDRAWN", "이미 탈퇴한 회원입니다."),
    MEMBER_WITHDRAWN(422, "MEMBER_WITHDRAWN", "탈퇴한 회원입니다.");

    private final int status;
    private final String code;
    private final String message;

    MemberErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
