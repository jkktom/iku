package org.mtvs.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Clerk OAuth user entity
@Entity
@Getter
@NoArgsConstructor
@Table(name = "clerk_users")
public class ClerkUser extends User {
    
    @Column(nullable = false, unique = true)
    private String clerkUserId;

    public ClerkUser(String email, String username, String clerkUserId) {
        super(email, username, Role.USER);
        this.clerkUserId = clerkUserId;
    }
}
