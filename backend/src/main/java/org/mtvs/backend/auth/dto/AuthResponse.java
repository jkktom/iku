package org.mtvs.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
}
