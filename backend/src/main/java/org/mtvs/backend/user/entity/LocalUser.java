package org.mtvs.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Local (email/password) user entity
@Entity
@Getter
@NoArgsConstructor
@Table(name = "local_users")
public class LocalUser extends User {
    
    @Column(nullable = false)
    private String password;

    public LocalUser(String email, String username, String password, Role role) {
        super(email, username, role);
        this.password = password;
    }
}