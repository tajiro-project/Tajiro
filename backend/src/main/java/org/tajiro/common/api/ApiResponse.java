package org.tajiro.common.api;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success; // 성공 여부 (true/false)
    private final String code;    // 응답 코드 (예: "COMMON_200", "PROP_404")
    private final String message; // 메시지
    private final T data;         // 실제 데이터 (에러 발생 시 null)

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // [성공 응답 생성 메서드]
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    // [에러 응답 생성 메서드 (ResponseCode 지정)]
    public static <T> ApiResponse<T> error(ErrorCode responseCode) {
        return new ApiResponse<>(false, responseCode.getCode(), responseCode.getMessage(), null);
    }

    // [에러 응답 생성 메서드 (메시지 커스텀)]
    public static <T> ApiResponse<T> error(ErrorCode responseCode, String customMessage) {
        return new ApiResponse<>(false, responseCode.getCode(), customMessage, null);
    }
}