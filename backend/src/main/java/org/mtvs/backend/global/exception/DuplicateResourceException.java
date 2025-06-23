package org.mtvs.backend.global.exception;

/**
 * 중복 리소스 예외
 */
public class DuplicateResourceException extends RuntimeException {
    private final String resourceType;
    private final String duplicateField;
    private final Object duplicateValue;
    
    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = null;
        this.duplicateField = null;
        this.duplicateValue = null;
    }
    
    public DuplicateResourceException(String resourceType, String duplicateField, Object duplicateValue) {
        super(String.format("%s의 %s '%s'이(가) 이미 존재합니다.", 
              resourceType, duplicateField, duplicateValue));
        this.resourceType = resourceType;
        this.duplicateField = duplicateField;
        this.duplicateValue = duplicateValue;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getDuplicateField() {
        return duplicateField;
    }
    
    public Object getDuplicateValue() {
        return duplicateValue;
    }
}
