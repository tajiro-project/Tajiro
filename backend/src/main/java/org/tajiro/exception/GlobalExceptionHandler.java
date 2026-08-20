package org.tajiro.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
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

    // 요청 바디 파싱 실패 (예: 생년월일 형식 오류) — 400으로 응답
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.valueOf(ErrorCode.INVALID_INPUT_VALUE.getStatus()))
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE));
    }

    // path variable/쿼리 파라미터 타입 불일치 (예: /api/terms/abc) — 400으로 응답
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.valueOf(ErrorCode.INVALID_INPUT_VALUE.getStatus()))
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        return ResponseEntity
                .status(HttpStatus.valueOf(ErrorCode.IMAGE_FILE_TOO_LARGE.getStatus()))
                .body(ApiResponse.error(ErrorCode.IMAGE_FILE_TOO_LARGE));
    }

    // 그 외 알 수 없는 서버 내부 시스템 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e,
            HttpServletRequest request) {
        log.error(
                "Unhandled server exception. method={}, uri={}",
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
