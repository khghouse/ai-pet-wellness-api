package io.github.khghouse.petwellness.domain.pet.exception;

import io.github.khghouse.common.core.global.exception.ErrorInfo;

public enum PetErrorCode implements ErrorInfo {
    BREED_NOT_FOUND(404, "BREED_NOT_FOUND", "견종을 찾을 수 없습니다."),
    BREED_INACTIVE(422, "BREED_INACTIVE", "비활성 견종은 선택할 수 없습니다."),
    PET_NOT_FOUND(404, "PET_NOT_FOUND", "반려견을 찾을 수 없습니다."),
    PET_MEMBERSHIP_FORBIDDEN(403, "PET_MEMBERSHIP_FORBIDDEN", "반려견 체중을 기록할 권한이 없습니다."),
    WEIGHT_INVALID(422, "WEIGHT_INVALID", "체중은 0.1kg 이상 999.9kg 이하이며 소수점 한 자리여야 합니다."),
    WEIGHT_MEASURED_AT_BEFORE_BIRTH_DATE(
            422, "WEIGHT_MEASURED_AT_BEFORE_BIRTH_DATE", "측정 시각은 반려견 생년월일 이후여야 합니다."),
    WEIGHT_MEASURED_AT_IN_FUTURE(422, "WEIGHT_MEASURED_AT_IN_FUTURE", "미래 시각의 체중은 기록할 수 없습니다.");

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
