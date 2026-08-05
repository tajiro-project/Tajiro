package org.tajiro.common.api;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // --- 성공 (200 OK) ---
    SUCCESS(200, "COMMON_200", "성공적으로 처리되었습니다."),

    // --- 클라이언트 에러 (400, 404) ---
    INVALID_INPUT_VALUE(400, "COMMON_400", "올바르지 않은 입력값입니다."),

    // --- 서버 에러 (500) ---
    INTERNAL_SERVER_ERROR(500, "SERVER_500", "서버 내부 오류가 발생했습니다."),

    // --- 매물 (Property) ---
    PROPERTY_NOT_FOUND(404, "PROP_404", "존재하지 않거나 삭제된 매물입니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}