package io.github.khghouse.petwellness.domain.member.exception;

import io.github.khghouse.common.core.global.exception.ErrorInfo;

public enum MemberErrorCode implements ErrorInfo {
    EMAIL_DUPLICATED(409, "EMAIL_DUPLICATED", "이미 가입된 이메일입니다."),
    LOGIN_FAILED(401, "LOGIN_FAILED", "이메일 또는 비밀번호가 일치하지 않습니다.");

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
