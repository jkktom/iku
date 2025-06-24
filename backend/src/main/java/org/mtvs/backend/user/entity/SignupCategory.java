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
@Table(name = "signup_categories")
public class SignupCategory {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    public SignupCategory(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final SignupCategory Local = new SignupCategory(1, "Local");
    public static final SignupCategory Clerk = new SignupCategory(2, "Clerk");
    public static final SignupCategory Naver = new SignupCategory(3, "Naver");
}
