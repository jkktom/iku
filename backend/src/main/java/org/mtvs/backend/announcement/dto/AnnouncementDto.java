package org.mtvs.backend.announcement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mtvs.backend.announcement.entity.Announcement;

public class AnnouncementDto {

    @Getter
    @NoArgsConstructor
    public static class Request {
        private String title;
        private String content;
        private boolean important;
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
            this.authorName = announcement.getAuthor().getUsername();
            this.important = announcement.isImportant();
            this.createdAt = announcement.getCreatedAt().toString();
        }
    }
}
