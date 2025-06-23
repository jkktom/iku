package org.mtvs.backend.global.exception;

/**
 * 잘못된 요청에 대한 예외
 */
public class BadRequestException extends RuntimeException {
    private final String field;
    private final Object rejectedValue;
    
    public BadRequestException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
    }
    
    public BadRequestException(String field, Object rejectedValue, String message) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getRejectedValue() {
        return rejectedValue;
    }
}
