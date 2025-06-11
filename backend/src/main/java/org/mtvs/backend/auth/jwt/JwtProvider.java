package org.mtvs.backend.auth.jwt;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final long validityInMs = 3600000; // 1시간

    /*
     * JWT 생성
     * */
    public String generateToken(String email) {
        log.info("[JWT 생성] 이메일 : {}", email);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        log.debug("[JWT 생성 완료] 만료시간 : {}", expiry);

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", List.of("ROLE_USER"))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    /*
     * JWT에서 이메일 추출
     * */
    public String getEmailFromToken(String token) {
        token = cleanToken(token);
        log.debug("[JWT 파싱] 이메일 추출 시작");

        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        String email = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        log.debug("[JWT 파싱 완료] 이메일: {}", email);
        return email;
    }

    /*
     * 토큰 유효성 검사
     * */
    public boolean validateToken(String token) {
        try {
            token = cleanToken(token);
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.debug("[JWT 유효성 검사] 유효성 검사 통과");
            return true;

        } catch (Exception e) {
            log.warn("[JWT 유효성 검사 실패] 에러: {}", e.getMessage());
            return false;
        }
    }

    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

}