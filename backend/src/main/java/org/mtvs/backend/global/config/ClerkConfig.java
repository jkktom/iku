package org.mtvs.backend.global.config;

import com.clerk.backend_api.Clerk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClerkConfig {

    @Value("${clerk.secret-key}")
    private String clerkSecretKey;

    @Bean
    public Clerk clerk() {
        return Clerk.builder()
            .bearerAuth(clerkSecretKey)
            .build();
    }
} 