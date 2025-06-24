package org.mtvs.backend.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignupRequest {
    private String email;
    private String username;
    private String password;
}