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

import java.util.Map;

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
     * 간단한 AI 테스트용 엔드포인트
     * Postman 테스트용
     */
    @PostMapping("/test")
    public ResponseEntity<?> testGeminiAI(@RequestBody Map<String, String> requestBody) {
        try {
            String message = requestBody.get("message");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("메시지가 비어있습니다.");
            }
            
            String aiResponse = geminiService.sendMessage(message);
            
            // 응답 구조화
            Map<String, Object> response = Map.of(
                "success", true,
                "userMessage", message,
                "aiResponse", aiResponse,
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 간단한 메시지 전송 (인증 없이)
     */
    @PostMapping("/simple")
    public ResponseEntity<?> sendSimpleMessage(@RequestBody String message) {
        try {
            String aiResponse = geminiService.sendMessage(message);
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }

    /**
     * 기존 메서드 (호환성 유지)
     */
    @PostMapping("/overallgame/analysis")
    public ResponseEntity<?> getGeminiResponse(
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        String geminiURL = geminiApiUrl;
        // GameData totalGameData = "totalGameData";

        String prompt = String.format("You are a helpful assistant \n" +
                 "that can answer LOL game questions.\n",
                 "totalGameData",
                 String.join(",")
        );

        Request request = new Request();
        request.createGeminiReqDto(prompt);
        System.out.println(prompt);

        String rawResponse = null;

        try {
            // Gemini API 호출
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