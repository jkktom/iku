package org.mtvs.backend.global.exception;

/**
 * 개선된 UnauthorizedException - 더 풍부한 정보 제공
 */
public class UnauthorizedException extends RuntimeException {
    private final String operation;
    private final String reason;
    
    public UnauthorizedException(String message) {
        super(message);
        this.operation = null;
        this.reason = null;
    }
    
    public UnauthorizedException(String operation, String reason) {
        super(String.format("%s 권한이 없습니다. 사유: %s", operation, reason));
        this.operation = operation;
        this.reason = reason;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getReason() {
        return reason;
    }
}
