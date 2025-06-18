package org.mtvs.backend.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "roles")
public class Role {
    
    @Id
    private Integer id;  // Fixed IDs: 1=GUEST, 2=USER, 3=ADMIN

    @Column(nullable = false, unique = true)
    private String name;  // "GUEST", "USER", "ADMIN"

    // Constructor for creating fixed roles
    protected Role(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final Role GUEST = new Role(1, "GUEST");
    public static final Role USER = new Role(2, "USER");
    public static final Role ADMIN = new Role(3, "ADMIN");
}
