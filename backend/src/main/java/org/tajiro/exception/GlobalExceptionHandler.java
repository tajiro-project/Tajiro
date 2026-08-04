package org.tajiro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 발생시킨 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode responseCode = e.getResponseCode();

        // ApiResponse.error(...) 로 감싸서 리턴
        return ResponseEntity
                .status(HttpStatus.valueOf(responseCode.getStatus()))
                .body(ApiResponse.error(responseCode));
    }

    // 그 외 알 수 없는 서버 내부 시스템 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}