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

    // --- AI 코칭 ---
    AI_API_KEY_NOT_CONFIGURED(503, "AI_503_1", "AI API 키가 설정되지 않았습니다."),
    AI_COACHING_UNAVAILABLE(503, "AI_503_2", "AI 코칭을 불러오지 못했어요. 잠시 후 다시 시도해주세요."),

    // --- 매물 (Property) ---
    PROPERTY_NOT_FOUND(404, "PROP_404", "존재하지 않거나 삭제된 매물입니다."),
    PROPERTY_IMAGE_NOT_FOUND(404, "PROP_IMAGE_404", "존재하지 않는 매물 이미지입니다."),
    INVALID_IMAGE_FILE(400, "PROP_IMAGE_400", "지원하지 않거나 올바르지 않은 이미지 파일입니다."),
    IMAGE_FILE_TOO_LARGE(413, "PROP_IMAGE_413", "이미지 파일은 10MB를 초과할 수 없습니다."),
    IMAGE_STORAGE_FAILED(500, "PROP_IMAGE_500", "이미지를 저장하지 못했습니다."),

    // --- 인프라 (Infrastructure) ---
    INFRASTRUCTURE_NOT_FOUND(404, "INFRA_404", "해당 매물의 인프라 정보가 존재하지 않습니다."),

    // --- 인증 (Auth) ---
    LOGIN_FAILED(401, "AUTH_401", "이메일 또는 비밀번호가 일치하지 않습니다."),
    AUTH_REQUIRED(401, "AUTH_401_1", "로그인이 필요합니다. 다시 로그인해 주세요."),
    INVALID_TOKEN(401, "AUTH_401_2", "유효하지 않은 토큰입니다."),
    EMAIL_DUPLICATE(409, "AUTH_409", "이미 사용 중인 이메일입니다."),
    REQUIRED_TERMS_NOT_AGREED(400, "AUTH_400", "필수 약관에 동의해야 합니다."),

    // --- 비교함 ---
    COMPARE_DUPLICATE(409, "COMPARE_409_1", "이미 비교함에 담긴 매물입니다."),
    COMPARE_LIMIT_EXCEEDED(409, "COMPARE_409_2", "비교함에는 매물을 최대 3개까지 담을 수 있습니다."),

    // --- 비교 리포트 ---
    COMPARISON_REPORT_NOT_FOUND(404, "REPORT_404", "존재하지 않는 비교 리포트입니다."),

    // --- 주거 선호 조건 ---
    PREFERENCE_NOT_FOUND(404, "PREF_404", "설정된 주거 선호 조건이 없습니다."),

    // --- 찜한 매물 ---
    FAVORITE_DUPLICATE(409, "FAV_409", "이미 찜한 매물입니다."),

    // --- 약관 ---
    TERMS_NOT_FOUND(404, "TERMS_404", "존재하지 않는 약관입니다."),

    // --- 임베딩 ---
    AI_EMBEDDING_UNAVAILABLE( 503, "AI_503_3","금융상품 임베딩 생성에 실패했습니다."),

    // --- 정책 배치 ---
    AI_POLICY_SUMMARY_UNAVAILABLE(503, "AI_503_4", "정책 요약 생성에 실패했습니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
