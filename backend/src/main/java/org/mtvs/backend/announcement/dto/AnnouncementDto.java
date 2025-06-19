package org.mtvs.backend.announcement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mtvs.backend.announcement.entity.Announcement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AnnouncementDto {

    @Getter
    @Setter
    @NoArgsConstructor
    // 기본 생성자 자동 생성
    public static class Request {
        private String title;
        private String content;
        private boolean important;
    }

    @Getter
    @Setter
    public static class Response {
        private final Long id;
        private final String title;
        private final String content;
        private final String authorName;
        private final boolean important;
        private final String createdAt;
        private final String updatedAt;
        private final String deletedAt;
        private final boolean isActive;

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public Response(Announcement announcement) {
            this.id = announcement.getId();
            this.title = announcement.getTitle() != null ? announcement.getTitle() : "";
            this.content = announcement.getContent() != null ? announcement.getContent() : "";
            this.authorName = (announcement.getAuthor() != null && announcement.getAuthor().getUsername() != null) 
                ? announcement.getAuthor().getUsername() 
                : "Unknown";
            this.important = announcement.isImportant();
            
            // 현재 시간을 기본값으로 사용
            LocalDateTime now = LocalDateTime.now();
            
            // null 안전성을 위한 처리 - 더 안전하게
            if (announcement.getCreatedAt() != null) {
                this.createdAt = announcement.getCreatedAt().format(formatter);
            } else {
                this.createdAt = now.format(formatter);
                // 로그로 문제 상황 기록
                System.out.println("WARNING: createdAt is null for announcement ID: " + announcement.getId());
            }
                
            if (announcement.getUpdatedAt() != null) {
                this.updatedAt = announcement.getUpdatedAt().format(formatter);
            } else {
                this.updatedAt = now.format(formatter); // null 대신 현재 시간 사용
            }
                
            if (announcement.getDeletedAt() != null) {
                this.deletedAt = announcement.getDeletedAt().format(formatter);
            } else {
                this.deletedAt = null;
            }
                
            this.isActive = announcement.isActive();
        }
    }
    
    public static Response fromEntity(Announcement announcement) {
        return new Response(announcement);
    }

}
