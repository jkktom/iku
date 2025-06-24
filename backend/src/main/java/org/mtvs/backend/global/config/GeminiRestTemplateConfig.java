package org.mtvs.backend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class GeminiRestTemplateConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Bean
    @Qualifier("geminiRestTemplate")
    public RestTemplate geminiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
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
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
}
