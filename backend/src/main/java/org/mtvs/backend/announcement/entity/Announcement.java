package org.mtvs.backend.announcement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mtvs.backend.global.entity.BaseEntity;
import org.mtvs.backend.user.entity.User;

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
}
