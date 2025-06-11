package org.mtvs.backend.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.mtvs.backend.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String roles;

    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.roles = "ROLE_USER";
    }
}