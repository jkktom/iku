package org.mtvs.backend.global.exception;

/**
 * 개선된 NotFoundException - 더 풍부한 정보 제공
 */
public class NotFoundException extends RuntimeException {
    private final String resourceType;
    private final String resourceId;
    
    public NotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }
    
    public NotFoundException(String resourceType, String resourceId) {
        super(String.format("%s을(를) 찾을 수 없습니다. ID: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public NotFoundException(String resourceType, Long resourceId) {
        this(resourceType, String.valueOf(resourceId));
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
}
