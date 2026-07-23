package io.github.khghouse.petwellness.domain.pet.exception;

import io.github.khghouse.common.core.global.exception.ErrorInfo;

public enum PetErrorCode implements ErrorInfo {
    BREED_NOT_FOUND(404, "BREED_NOT_FOUND", "견종을 찾을 수 없습니다."),
    BREED_INACTIVE(422, "BREED_INACTIVE", "비활성 견종은 선택할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    PetErrorCode(int status, String code, String message) {
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
