package org.mtvs.backend.auth.controller;

import org.mtvs.backend.auth.dto.SignupRequest;
import org.mtvs.backend.auth.dto.LoginRequest;
import org.mtvs.backend.auth.dto.AuthResponse;
import org.mtvs.backend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /*
     * 회원가입
     * */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest dto) {
        try {
            authService.signup(dto);
            return ResponseEntity.ok("회원가입 성공");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/local-login")
    public ResponseEntity<?> localLogin(@RequestBody LoginRequest dto) {
        try {
            AuthResponse response = authService.login(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("로그인 실패: " + e.getMessage());
        }
    }

    @PostMapping("/clerk-login")
    public ResponseEntity<?> clerkLogin(HttpServletRequest request) {
        try {
            AuthResponse response = authService.clerkLogin(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Clerk 로그인 실패: " + e.getMessage());
        }
    }
}
