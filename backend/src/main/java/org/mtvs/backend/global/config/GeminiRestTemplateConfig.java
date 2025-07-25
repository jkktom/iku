package org.mtvs.backend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class GeminiRestTemplateConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Bean
    @Qualifier("geminiRestTemplate")
    public RestTemplate geminiRestTemplate(RestTemplateBuilder builder) {
        // Spring Boot 3.x 방식으로 타임아웃 설정
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))    // 5초 → 10초
                .setReadTimeout(Duration.ofMinutes(5))        // 120초 → 300초 (5분)
                .build();
        
        // API 키를 헤더에 추가하는 인터셉터
        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set("x-goog-api-key", geminiApiKey);
            headers.set("Content-Type", "application/json");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }

    @Bean
    @Qualifier("defaultRestTemplate")
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // 일반적인 API 호출용 타임아웃 설정
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))    // 3초 → 10초
                .setReadTimeout(Duration.ofMinutes(2))        // 10초 → 120초
                .build();
    }
}
