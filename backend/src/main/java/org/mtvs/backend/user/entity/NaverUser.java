package org.mtvs.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "naver_users")
public class NaverUser extends User {
    
    @Column(nullable = false, unique = true)
    private String naverUserId;

    public NaverUser(String email, String username, String naverUserId) {
        super(email, username, Role.USER);
        this.naverUserId = naverUserId;
    }
}