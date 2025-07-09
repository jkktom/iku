package org.mtvs.backend.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.analysis.dto.AIAnalysisResponseDto;
import org.mtvs.backend.analysis.entity.MatchAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AnalysisResponseBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisResponseBuilder.class);
    
    private final ObjectMapper objectMapper;
    
    public AnalysisResponseBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public AIAnalysisResponseDto buildResponse(MatchAnalysis analysis) {
        try {
            AIAnalysisResponseDto response = new AIAnalysisResponseDto();
            response.setAnalysisId(analysis.getId());
            response.setPuuid(analysis.getPuuid());
            response.setMatchId(analysis.getMatchId());
            response.setTargetPlayerName(analysis.getTargetPlayerName());
            response.setTargetChampion(analysis.getTargetChampion());
            response.setAnalysisStatus(analysis.getAnalysisStatus());
            response.setAnalysisSummary(analysis.getAnalysisSummary());
            response.setErrorMessage(analysis.getErrorMessage());
            response.setCreatedAt(analysis.getCreatedAt());
            response.setUpdatedAt(analysis.getUpdatedAt());
            // Note: MatchAnalysis doesn't have completedAt field
            // response.setCompletedAt(analysis.getCompletedAt());
            
            // Note: MatchAnalysis doesn't have analysisDurationSeconds field  
            // if (analysis.getAnalysisDurationSeconds() != null) {
            //     response.setAnalysisDurationSeconds(analysis.getAnalysisDurationSeconds());
            // }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error building analysis response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to build analysis response: " + e.getMessage());
        }
    }
    
    public Map<String, Object> buildDetailedResponse(MatchAnalysis analysis) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("analysisId", analysis.getId());
        response.put("puuid", analysis.getPuuid());
        response.put("matchId", analysis.getMatchId());
        response.put("status", analysis.getAnalysisStatus());
        response.put("aiResponse", analysis.getAnalysisSummary()); // MatchAnalysis uses analysisSummary
        response.put("errorMessage", analysis.getErrorMessage());
        response.put("createdAt", analysis.getCreatedAt());
        response.put("updatedAt", analysis.getUpdatedAt());
        // Note: MatchAnalysis doesn't have completedAt or analysisDurationSeconds fields
        // response.put("completedAt", analysis.getCompletedAt());
        // response.put("analysisDurationSeconds", analysis.getAnalysisDurationSeconds());
        
        // Add parsed AI request if available
        if (analysis.getAiRequestData() != null) {
            try {
                response.put("aiRequest", analysis.getAiRequestData());
            } catch (Exception e) {
                logger.warn("Failed to parse AI request: {}", e.getMessage());
                response.put("aiRequest", analysis.getAiRequestData());
            }
        }
        
        return response;
    }
    
    private Object parseAiRequest(String aiRequest) {
        try {
            return objectMapper.readValue(aiRequest, Object.class);
        } catch (Exception e) {
            // Return as string if not valid JSON
            return aiRequest;
        }
    }
    
    public Map<String, Object> buildErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    public Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}