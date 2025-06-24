package org.mtvs.backend.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON에 포함하지 않음
public class ErrorResponse {
    
    /**
     * HTTP 상태 코드
     */
    private int status;
    
    /**
     * 에러 코드 (비즈니스 로직용)
     */
    private String error;
    
    /**
     * 에러 메시지 (사용자에게 표시될 메시지)
     */
    private String message;
    
    /**
     * 요청 경로
     */
    private String path;
    
    /**
     * 발생 시각
     */
    private LocalDateTime timestamp;
    
    /**
     * 필드별 검증 오류 (Validation 실패 시에만 사용)
     */
    private Map<String, String> fieldErrors;
    
    /**
     * 추가 디버그 정보 (개발환경에서만 사용)
     */
    private String debugMessage;

    // ================================
    // 정적 팩토리 메서드들
    // ================================
    
    public static ErrorResponse of(int status, String error, String message) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse withFieldErrors(int status, String error, String message, 
                                              Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
