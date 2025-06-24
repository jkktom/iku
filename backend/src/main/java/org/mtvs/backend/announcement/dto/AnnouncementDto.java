package org.mtvs.backend.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mtvs.backend.announcement.entity.Announcement;
import org.mtvs.backend.announcement.validation.ValidAnnouncementTitle;

public class AnnouncementDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하로 입력해주세요.")
        @ValidAnnouncementTitle
        private String title;
        
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하로 입력해주세요.")
        private String content;
        
        @NotNull(message = "중요도 설정은 필수입니다.")
        private Boolean important;

        // 기존 getter 메서드들 (boolean -> Boolean 변경으로 인한 수정)
        public boolean isImportant() {
            return important != null && important;
        }

        // 생성자 추가 (테스트 편의성)
        public Request(String title, String content, Boolean important) {
            this.title = title;
            this.content = content;
            this.important = important;
        }
    }

    @Getter
    public static class Response {
        private final Long id;
        private final String title;
        private final String content;
        private final String authorName;
        private final boolean important;
        private final String createdAt;

        public Response(Announcement announcement) {
            this.id = announcement.getId();
            this.title = announcement.getTitle();
            this.content = announcement.getContent();
            this.authorName = announcement.getAuthor() != null ? announcement.getAuthor().getUsername() : "시스템";
            this.important = announcement.isImportant();
            // null 체크 추가
            this.createdAt = announcement.getCreatedAt() != null 
                ? announcement.getCreatedAt().toString() 
                : java.time.LocalDateTime.now().toString();
        }
    }

    // 누락된 fromEntity 메서드 추가
    public static Response fromEntity(Announcement announcement) {
        return new Response(announcement);
    }
}