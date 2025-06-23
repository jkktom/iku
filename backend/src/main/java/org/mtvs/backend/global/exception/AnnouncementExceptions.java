package org.mtvs.backend.global.exception;

/**
 * 공지사항 관련 커스텀 예외들
 */
public class AnnouncementExceptions {

    /**
     * 공지사항을 찾을 수 없을 때
     */
    public static class AnnouncementNotFoundException extends NotFoundException {
        public AnnouncementNotFoundException(Long id) {
            super("공지사항을 찾을 수 없습니다. ID: " + id);
        }

        public AnnouncementNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * 공지사항 제목이 너무 길 때
     */
    public static class TitleTooLongException extends RuntimeException {
        public TitleTooLongException(int maxLength) {
            super("공지사항 제목은 " + maxLength + "자를 초과할 수 없습니다.");
        }
    }

    /**
     * 공지사항 내용이 너무 길 때
     */
    public static class ContentTooLongException extends RuntimeException {
        public ContentTooLongException(int maxLength) {
            super("공지사항 내용은 " + maxLength + "자를 초과할 수 없습니다.");
        }
    }

    /**
     * 이미 삭제된 공지사항에 접근할 때
     */
    public static class DeletedAnnouncementException extends RuntimeException {
        public DeletedAnnouncementException(Long id) {
            super("삭제된 공지사항입니다. ID: " + id);
        }
    }

    /**
     * 중복된 공지사항 제목일 때 (필요한 경우)
     */
    public static class DuplicateTitleException extends RuntimeException {
        public DuplicateTitleException(String title) {
            super("동일한 제목의 공지사항이 이미 존재합니다: " + title);
        }
    }

    /**
     * 공지사항 수정 권한이 없을 때
     */
    public static class AnnouncementAccessDeniedException extends UnauthorizedException {
        public AnnouncementAccessDeniedException(Long announcementId, Long userId) {
            super("공지사항 수정 권한이 없습니다. 공지사항 ID: " + announcementId + ", 사용자 ID: " + userId);
        }
    }

    /**
     * 잘못된 정렬 필드를 요청했을 때
     */
    public static class InvalidSortFieldException extends RuntimeException {
        public InvalidSortFieldException(String field) {
            super("지원하지 않는 정렬 필드입니다: " + field + ". 사용 가능한 필드: id, title, createdAt, important");
        }
    }

    /**
     * 페이지 크기가 허용 범위를 벗어날 때
     */
    public static class InvalidPageSizeException extends RuntimeException {
        public InvalidPageSizeException(int size, int max) {
            super("페이지 크기가 허용 범위를 벗어났습니다. 요청: " + size + ", 최대: " + max);
        }
    }
}