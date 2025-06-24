package org.mtvs.backend.announcement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class AnnouncementTitleValidator implements ConstraintValidator<ValidAnnouncementTitle, String> {

    @Override
    public void initialize(ValidAnnouncementTitle constraintAnnotation) {
        // 초기화 로직이 필요한 경우 구현
    }

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        // null이나 빈 문자열은 @NotBlank가 처리하므로 여기서는 통과
        if (!StringUtils.hasText(title)) {
            return true;
        }

        String trimmedTitle = title.trim();

        // 1. 길이 검증 (2-100자)
        if (trimmedTitle.length() < 2 || trimmedTitle.length() > 100) {
            addConstraintViolation(context, "제목은 2자 이상 100자 이하로 입력해주세요.");
            return false;
        }

        // 2. HTML 태그 검증
        if (containsHtmlTags(trimmedTitle)) {
            addConstraintViolation(context, "제목에는 HTML 태그를 사용할 수 없습니다.");
            return false;
        }

        // 3. 특수문자 제한 (기본적인 특수문자만 허용)
        if (!isValidCharacters(trimmedTitle)) {
            addConstraintViolation(context, "제목에 허용되지 않는 특수문자가 포함되어 있습니다.");
            return false;
        }

        // 4. 연속된 공백 검증
        if (hasConsecutiveSpaces(trimmedTitle)) {
            addConstraintViolation(context, "제목에 연속된 공백은 사용할 수 없습니다.");
            return false;
        }

        return true;
    }

    private boolean containsHtmlTags(String text) {
        return text.matches(".*<[^>]+>.*");
    }

    private boolean isValidCharacters(String text) {
        // 한글, 영문, 숫자, 기본 특수문자(공백, 점, 쉼표, 느낌표, 물음표, 괄호, 하이픈)만 허용
        return text.matches("^[가-힣a-zA-Z0-9\\s.,!?()\\-_:]+$");
    }

    private boolean hasConsecutiveSpaces(String text) {
        return text.contains("  "); // 두 개 이상의 연속된 공백
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}