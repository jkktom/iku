package org.mtvs.backend.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ErrorResponse 생성을 위한 헬퍼 클래스 (코드 중복 제거)
 */
public class ErrorResponseHelper {
    
    /**
     * 기본 에러 응답 생성
     */
    public static ErrorResponse createErrorResponse(HttpStatus status, String errorCode, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(errorCode)
                .message(message)
                .path(getCurrentPath())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 필드 에러가 포함된 검증 실패 응답 생성
     */
    public static ErrorResponse createValidationErrorResponse(String message, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_FAILED")
                .message(message)
                .fieldErrors(fieldErrors)
                .path(getCurrentPath())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 리소스 없음 응답 생성
     */
    public static ErrorResponse createNotFoundResponse(String message) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
    
    /**
     * 권한 없음 응답 생성
     */
    public static ErrorResponse createForbiddenResponse(String message) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }
    
    /**
     * 인증 실패 응답 생성
     */
    public static ErrorResponse createUnauthorizedResponse(String message) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", message);
    }
    
    /**
     * 잘못된 요청 응답 생성
     */
    public static ErrorResponse createBadRequestResponse(String errorCode, String message) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorCode, message);
    }
    
    /**
     * 서버 내부 오류 응답 생성
     */
    public static ErrorResponse createInternalServerErrorResponse(String message) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", message);
    }
    
    /**
     * 현재 요청 경로 가져오기
     */
    private static String getCurrentPath() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest().getRequestURI();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
