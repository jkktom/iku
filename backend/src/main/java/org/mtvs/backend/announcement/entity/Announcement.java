package org.mtvs.backend.announcement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mtvs.backend.global.entity.BaseEntity;
import org.mtvs.backend.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private boolean important;

    public Announcement(String title, String content, User author, boolean important) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.important = important;
    }

    // 누락된 updateInfo 메서드 추가
    public void updateInfo(String title, String content, boolean important) {
        this.title = title;
        this.content = content;
        this.important = important;
    }

    // JPA Auditing이 작동하지 않는 경우를 대비한 수동 설정
    @PrePersist
    public void prePersist() {
        if (super.getCreatedAt() == null) {
            // BaseEntity의 createdAt이 null인 경우 수동으로 설정
            // 하지만 BaseEntity는 protected이므로 여기서는 JPA Auditing에 의존
        }
    }
}