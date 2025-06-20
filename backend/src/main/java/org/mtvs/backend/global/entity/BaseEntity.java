package org.mtvs.backend.global.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * JPA Auditing이 작동하지 않을 경우를 대비한 수동 날짜 설정
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티를 비활성화(소프트 삭제)합니다.
     */
    public void inactive() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티가 활성 상태인지 확인합니다.
     * @return 삭제되지 않았으면 true, 삭제되었으면 false
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * 엔티티를 다시 활성화합니다.
     */
    public void activate() {
        this.deletedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티를 영구 삭제합니다. (deletedAt을 현재 시간으로 설정)
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}