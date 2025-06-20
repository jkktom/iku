package org.mtvs.backend.auth.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.mtvs.backend.auth.security.CustomUserDetails;
import org.mtvs.backend.auth.util.JwtUtil;
import org.mtvs.backend.user.entity.User;
import org.mtvs.backend.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        log.info("[JWT 필터] 요청 URI : {}, Authorization 헤더 존재: {}", requestURI, authHeader != null);

        // 공지사항 API는 JWT 필터를 건너뛰고 바로 진행 (개발용)
        if (requestURI.startsWith("/api/announcements")) {
            log.info("[JWT 필터] 공지사항 API 요청 - JWT 검증 건너뛰기");
            chain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("[JWT 필터] Authorization 헤더에서 토큰 추출 성공: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

            try {
                if (jwtUtil.validateToken(token)) {
                    // Check if it's a guest token
                    if (jwtUtil.isGuestToken(token)) {
                        // For guest tokens, create a simple authentication with guest role
                        Authentication guestAuth = new UsernamePasswordAuthenticationToken(
                                "guest", null, List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
                        );
                        SecurityContextHolder.getContext().setAuthentication(guestAuth);
                        log.info("[JWT 필터] 게스트 토큰 인증 완료");
                    } else {
                        // Regular user token handling
                        String username = jwtUtil.getUsername(token);
                        log.info("[JWT 필터] 토큰 검증 성공 : Username={}", username);

                        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> {
                                    log.warn("[JWT 필터] 사용자 Username DB 조회 실패 : {}", username);
                                    return new RuntimeException("유저 없음");
                                });

                        CustomUserDetails userDetails = new CustomUserDetails(user);

                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("[JWT 필터] SecurityContext 인증 완료 : 사용자 UUID={}, 사용자 username={}",
                                user.getId(), user.getUsername());
                    }
                } else {
                    log.warn("[JWT 필터] 토큰 유효성 검사 실패");
                }
            } catch (Exception e) {
                log.error("[JWT 필터] 토큰 처리 중 오류 발생 : {}", e.getMessage());
            }
        } else {
            log.info("[JWT 필터] Authorization 헤더 토큰 없음 - 인증 없이 계속 진행");
        }
        chain.doFilter(request, response);
    }
}