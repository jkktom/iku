package org.mtvs.backend.announcement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mtvs.backend.global.entity.BaseEntity;
import org.mtvs.backend.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private boolean important = false;

    public Announcement(String title, String content, User author, boolean important) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.important = important;
        
        // 수동으로 날짜 설정 (JPA Auditing 백업)
        LocalDateTime now = LocalDateTime.now();
        if (this.getCreatedAt() == null) {
            this.setCreatedAt(now);
        }
        if (this.getUpdatedAt() == null) {
            this.setUpdatedAt(now);
        }
    }

    /**
     * 공지사항 정보를 업데이트합니다.
     */
    public void updateInfo(String title, String content, boolean important) {
        this.title = title;
        this.content = content;
        this.important = important;
        
        // 수정 시간 업데이트
        this.setUpdatedAt(LocalDateTime.now());
    }
}
