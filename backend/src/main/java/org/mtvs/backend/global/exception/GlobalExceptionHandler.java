package org.mtvs.backend.global.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 개발 환경에서만 상세 정보 노출
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;
    
    private boolean isDevelopment() {
        return "dev".equals(activeProfile) || "local".equals(activeProfile);
    }

    // ================================
    // 1. 비즈니스 로직 예외 (ErrorResponseHelper 활용)
    // ================================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseHelper.createNotFoundResponse(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseHelper.createNotFoundResponse(e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        log.warn("Unauthorized access: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseHelper.createForbiddenResponse(e.getMessage()));
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("BAD_REQUEST", e.getMessage()));
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException e) {
        log.warn("Duplicate resource: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseHelper.createErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", e.getMessage()));
    }

    // ================================
    // Announcement 커스텀 예외들
    // ================================

    @ExceptionHandler(AnnouncementExceptions.AnnouncementNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAnnouncementNotFound(AnnouncementExceptions.AnnouncementNotFoundException e) {
        log.warn("Announcement not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseHelper.createErrorResponse(HttpStatus.NOT_FOUND, "ANNOUNCEMENT_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler({
            AnnouncementExceptions.TitleTooLongException.class,
            AnnouncementExceptions.ContentTooLongException.class
    })
    public ResponseEntity<ErrorResponse> handleContentTooLong(RuntimeException e) {
        log.warn("Content too long: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("CONTENT_TOO_LONG", e.getMessage()));
    }

    @ExceptionHandler(AnnouncementExceptions.DeletedAnnouncementException.class)
    public ResponseEntity<ErrorResponse> handleDeletedAnnouncement(AnnouncementExceptions.DeletedAnnouncementException e) {
        log.warn("Deleted announcement access: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ErrorResponseHelper.createErrorResponse(HttpStatus.GONE, "DELETED_RESOURCE", e.getMessage()));
    }

    @ExceptionHandler(AnnouncementExceptions.DuplicateTitleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTitle(AnnouncementExceptions.DuplicateTitleException e) {
        log.warn("Duplicate title: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseHelper.createErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_TITLE", e.getMessage()));
    }

    @ExceptionHandler(AnnouncementExceptions.AnnouncementAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAnnouncementAccessDenied(AnnouncementExceptions.AnnouncementAccessDeniedException e) {
        log.warn("Announcement access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseHelper.createErrorResponse(HttpStatus.FORBIDDEN, "ANNOUNCEMENT_ACCESS_DENIED", e.getMessage()));
    }

    @ExceptionHandler({
            AnnouncementExceptions.InvalidSortFieldException.class,
            AnnouncementExceptions.InvalidPageSizeException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidRequest(RuntimeException e) {
        log.warn("Invalid request parameter: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("INVALID_REQUEST_PARAMETER", e.getMessage()));
    }

    // ================================
    // 2. 데이터 검증 예외 (코드 간소화)
    // ================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createValidationErrorResponse("입력값 검증에 실패했습니다.", fieldErrors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.warn("Bind exception: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createValidationErrorResponse("요청 데이터 바인딩에 실패했습니다.", fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("CONSTRAINT_VIOLATION", "데이터 제약조건 위반입니다."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("HTTP message not readable: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("INVALID_JSON", "잘못된 JSON 형식입니다."));
    }

    // ================================
    // 3. HTTP 관련 예외 (코드 간소화)
    // ================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: parameter '{}' with value '{}'", e.getName(), e.getValue());

        String message = String.format("파라미터 '%s'의 값 '%s'이(가) 올바른 형식이 아닙니다.",
                e.getName(), e.getValue());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("TYPE_MISMATCH", message));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleNumberFormat(NumberFormatException e) {
        log.warn("Number format exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("INVALID_NUMBER_FORMAT", "숫자 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponseHelper.createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getMessage());

        String message = String.format("필수 파라미터 '%s'이(가) 누락되었습니다.", e.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseHelper.createBadRequestResponse("MISSING_PARAMETER", message));
    }

    // ================================
    // 4. 데이터베이스 예외
    // ================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseHelper.createErrorResponse(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "데이터 무결성 제약조건을 위반했습니다."));
    }

    // ================================
    // 5. 보안 관련 예외
    // ================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseHelper.createForbiddenResponse("접근 권한이 없습니다."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseHelper.createUnauthorizedResponse("인증에 실패했습니다."));
    }

    // ================================
    // 6. 기타 예외 (최후의 방어선)
    // ================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        log.error("Unexpected error occurred: ", e);
        
        // 개발 환경에서는 상세 정보 포함
        String message = isDevelopment() 
            ? String.format("서버 내부 오류가 발생했습니다. %s", e.getMessage())
            : "서버 내부 오류가 발생했습니다.";
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseHelper.createInternalServerErrorResponse(message));
    }
}
