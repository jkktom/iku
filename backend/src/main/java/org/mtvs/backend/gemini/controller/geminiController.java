package org.mtvs.backend.gemini.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.auth.security.CustomUserDetails;
import org.mtvs.backend.gemini.dto.Request;
import org.mtvs.backend.gemini.dto.Response;
import org.mtvs.backend.gemini.service.GeminiService;
import org.mtvs.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/gemini")
public class geminiController {
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public geminiController(RestTemplate restTemplate, UserRepository userRepository, GeminiService geminiService) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 전체 게임 분석
     */
    @PostMapping("/overallgame/analysis")
    public ResponseEntity<?> getGeminiResponse(
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        String geminiURL = geminiApiUrl;

        String prompt = String.format("You are a helpful assistant \n" +
                 "that can answer LOL game questions.\n",
                 "totalGameData",
                 String.join(",")
        );

        Request request = new Request();
        request.createGeminiReqDto(prompt);

        String rawResponse = null;

        try {
            Response response = restTemplate.postForObject(geminiURL, request, Response.class);
            rawResponse = response.getCandidates().get(0).getContent().getParts().get(0).getText();
            
            return ResponseEntity.ok(rawResponse);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(rawResponse);
            return ResponseEntity.ok(rawResponse);
        }
    }
}
