package org.mtvs.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "signup_category_id", nullable = false)
    private SignupCategory signupCategory;

    // For OAuth users, this is the provider's user ID (e.g., Clerk, Naver)
    @Column(nullable = true)
    private String linkingUserId;

    // For local users, this is the password hash
    @Column(nullable = true)
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    // Main constructor for all users
    @Builder
    public User(String email, String username, Role role, SignupCategory signupCategory, String linkingUserId, String password) {
        this.email = email;
        this.username = username;
        this.role = role;
        this.signupCategory = signupCategory;
        this.linkingUserId = linkingUserId;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 사용자 정보를 업데이트합니다.
     */
    public void updateInfo(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /**
     * 비밀번호를 변경합니다.
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 사용자 역할을 변경합니다.
     */
    public void changeRole(Role newRole) {
        this.role = newRole;
    }


    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
