package org.mtvs.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Support multiple environments with environment-specific configurations
        List<String> allowedOrigins = List.of(
                // Development environments
                "http://localhost:*",
                "http://127.0.0.1:*",

                // Production domains
                "https://*.shaunthesheep.store",
                "https://shaunthesheep.store",
                "https://api.shaunthesheep.store",
                "https://dev.shaunthesheep.store",
                "https://dev-api.shaunthesheep.store",
                "http://localhost:3000",
                "http://localhost:8080",

                // Vercel deployment domains
                "https://*.vercel.app",
                "https://shaun-the-sheep-ai.vercel.app",
                "https://shaun-the-sheep-ai-*.vercel.app"
        );

        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1 hour preflight cache

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/health").permitAll()

                        // Guest endpoints (accessible with guest token)
                        .requestMatchers("/api/guest/**").hasAnyRole("GUEST", "USER")
                        .requestMatchers("/api/checklist/guest").hasAnyRole("GUEST", "USER")
                        .requestMatchers("/api/recommend/guest").hasAnyRole("GUEST", "USER")
                        .requestMatchers("/api/recommend/diagnoses").hasAnyRole("GUEST", "USER")

                        // Protected endpoints (requires full user authentication)
                        .requestMatchers("/api/profile").hasRole("USER")
                        .requestMatchers("/api/naver").hasRole("USER")
                        .requestMatchers("/api/recommend/**").hasRole("USER")
                        .requestMatchers("/api/deep/**").hasRole("USER")
                        .requestMatchers("/api/routine/**").hasRole("USER")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .requestMatchers("/api/chat-messages/ask").hasRole("USER")
                        .requestMatchers("/api/checklist/**").hasRole("USER")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}