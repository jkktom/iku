package org.mtvs.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mtvs.backend.global.entity.BaseEntity;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public enum Role {
        USER, ADMIN, GUEST
    }

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
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
}