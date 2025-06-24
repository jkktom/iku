package org.mtvs.backend.gemini.service;

import org.mtvs.backend.gemini.dto.Request;
import org.mtvs.backend.gemini.dto.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiBaseUrl;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 간단한 텍스트를 Gemini AI에게 보내고 응답 받기
     */
    public String sendMessage(String message) {
        try {
            // API Key가 설정되어 있는지 확인
            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                return "API Key가 설정되지 않았습니다. application.yml 확인해주세요.";
            }
            
            // URL에 API Key 포함
            String url = UriComponentsBuilder.fromHttpUrl(geminiBaseUrl)
                    .queryParam("key", geminiApiKey)
                    .toUriString();
            
            // 요청 객체 생성
            Request request = new Request();
            request.createGeminiReqDto(message);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Request> entity = new HttpEntity<>(request, headers);
            
            System.out.println("Gemini API 호출 URL: " + url);
            System.out.println("전송 메시지: " + message);
            System.out.println("API Key (일부): " + geminiApiKey.substring(0, Math.min(10, geminiApiKey.length())) + "...");
            
            // 먼저 원시 응답을 String으로 받아서 확인
            String rawResponse = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            ).getBody();
            
            System.out.println("=== 원시 응답 확인 ===");
            System.out.println(rawResponse);
            System.out.println("=== 원시 응답 끝 ===");
            
            // 그 다음 Response 객체로 파싱
            Response response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Response.class
            ).getBody();
            
            // 응답 디버깅 로그 추가
            System.out.println("=== Gemini API 응답 디버깅 ===");
            System.out.println("응답 객체: " + response);
            
            if (response != null) {
                System.out.println("Candidates 존재: " + (response.getCandidates() != null));
                if (response.getCandidates() != null) {
                    System.out.println("Candidates 크기: " + response.getCandidates().size());
                    if (!response.getCandidates().isEmpty()) {
                        System.out.println("첫 번째 Candidate: " + response.getCandidates().get(0));
                        if (response.getCandidates().get(0).getContent() != null) {
                            System.out.println("Content 존재: true");
                            if (response.getCandidates().get(0).getContent().getParts() != null) {
                                System.out.println("Parts 크기: " + response.getCandidates().get(0).getContent().getParts().size());
                                if (!response.getCandidates().get(0).getContent().getParts().isEmpty()) {
                                    System.out.println("첫 번째 Part: " + response.getCandidates().get(0).getContent().getParts().get(0));
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("=== 디버깅 끝 ===");
            
            // 응답 처리
            if (response != null && 
                response.getCandidates() != null && 
                !response.getCandidates().isEmpty() &&
                response.getCandidates().get(0).getContent() != null &&
                response.getCandidates().get(0).getContent().getParts() != null &&
                !response.getCandidates().get(0).getContent().getParts().isEmpty()) {
                
                String aiResponse = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                System.out.println("AI 응답: " + aiResponse);
                return aiResponse;
            }
            
            return "AI로부터 응답을 받을 수 없습니다.";
            
        } catch (Exception e) {
            System.err.println("Gemini API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            // 더 자세한 오류 정보 제공
            if (e.getMessage().contains("403")) {
                return "API Key 인증 실패: API Key를 확인해주세요.";
            } else if (e.getMessage().contains("401")) {
                return "API Key가 유효하지 않습니다.";
            } else if (e.getMessage().contains("429")) {
                return "API 호출 한도를 초과했습니다.";
            }
            
            return "오류가 발생했습니다: " + e.getMessage();
        }
    }
}