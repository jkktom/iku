package org.mtvs.backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.mtvs.backend.auth.dto.SignupRequest;
import org.mtvs.backend.auth.dto.LoginRequest;
import org.mtvs.backend.auth.dto.AuthResponse;
import org.mtvs.backend.auth.util.ClerkJwtVerifier;
import org.mtvs.backend.auth.util.JwtUtil;
import org.mtvs.backend.user.entity.User;
import org.mtvs.backend.user.entity.Role;
import org.mtvs.backend.user.entity.SignupCategory;
import org.mtvs.backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ClerkJwtVerifier clerkJwtVerifier;

    public User signup(SignupRequest dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        User user = User.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .signupCategory(SignupCategory.Local)
                .build();
        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest dto) {
        Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
        User user = userOpt.get();
        if (user.getPassword() == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId().toString());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse clerkLogin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);

        try {
            var claims = clerkJwtVerifier.verifyAndGetClaims(token);
            String clerkUserId = claims.getSubject();
            String email = claims.getStringClaim("email");
            String username = claims.getStringClaim("username");
            if (username == null || username.isEmpty()) {
                username = email.split("@")[0];
            }

            Optional<User> userOpt = userRepository.findByLinkingUserId(clerkUserId);
            User user;
            if (userOpt.isEmpty()) {
                user = User.builder()
                        .email(email)
                        .username(username)
                        .role(Role.USER)
                        .signupCategory(SignupCategory.Clerk)
                        .linkingUserId(clerkUserId)
                        .build();
                userRepository.save(user);
            } else {
                user = userOpt.get();
            }
            String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId().toString());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            return new AuthResponse(accessToken, refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Clerk token: " + e.getMessage());
        }
    }
}
