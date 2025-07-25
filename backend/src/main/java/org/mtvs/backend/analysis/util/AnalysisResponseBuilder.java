package org.mtvs.backend.analysis.util;

import org.mtvs.backend.analysis.entity.BaseAnalysis;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 분석 결과 응답을 구성하는 유틸리티 클래스
 */
@Component
public class AnalysisResponseBuilder {
    
    /**
     * 기본 응답 구조 생성
     */
    public Map<String, Object> buildBaseResponse(BaseAnalysis analysis) {
        Map<String, Object> response = new HashMap<>();
        
        // 기본 정보
        response.put("id", analysis.getId());
        response.put("puuid", analysis.getPuuid());
        response.put("targetPlayerName", analysis.getTargetPlayerName());
        response.put("status", analysis.getAnalysisStatus().name());
        response.put("createdAt", analysis.getCreatedAt());
        response.put("updatedAt", analysis.getUpdatedAt());
        
        // 분석 결과
        if (analysis.getAnalysisSummary() != null) {
            response.put("analysisSummary", analysis.getAnalysisSummary());
        }
        
        if (analysis.getAiResponseData() != null) {
            response.put("aiResponseData", analysis.getAiResponseData());
        }
        
        // 에러 정보 (있는 경우)
        if (analysis.getErrorMessage() != null) {
            response.put("errorMessage", analysis.getErrorMessage());
        }
        
        // 분석 상태 관련 추가 정보
        response.put("isCompleted", analysis.isCompleted());
        response.put("isFailed", analysis.isFailed());
        response.put("isProcessing", analysis.isProcessing());
        response.put("isRequested", analysis.isRequested());
        
        return response;
    }
    
    /**
     * 분석별 특수 데이터를 기본 응답에 추가
     */
    public Map<String, Object> addSpecificData(Map<String, Object> baseResponse, Map<String, Object> specificData) {
        if (specificData != null && !specificData.isEmpty()) {
            baseResponse.putAll(specificData);
        }
        return baseResponse;
    }
    
    /**
     * 페이징 정보를 포함한 응답 생성
     */
    public Map<String, Object> buildPagedResponse(Map<String, Object> data, int page, int size, long totalElements) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", data);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalElements);
        response.put("totalPages", (int) Math.ceil((double) totalElements / size));
        response.put("first", page == 0);
        response.put("last", page >= Math.ceil((double) totalElements / size) - 1);
        
        return response;
    }
    
    /**
     * 성공 응답 래퍼
     */
    public Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 에러 응답 래퍼
     */
    public Map<String, Object> buildErrorResponse(String message, String errorCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", Map.of(
            "message", message,
            "code", errorCode,
            "timestamp", System.currentTimeMillis()
        ));
        
        return response;
    }
    
    /**
     * 분석 통계 정보 구성
     */
    public Map<String, Object> buildAnalysisStats(long totalCount, long completedCount, long failedCount, long processingCount) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("completedCount", completedCount);
        stats.put("failedCount", failedCount);
        stats.put("processingCount", processingCount);
        stats.put("pendingCount", totalCount - completedCount - failedCount - processingCount);
        
        // 완료율 계산
        if (totalCount > 0) {
            stats.put("completionRate", Math.round((double) completedCount / totalCount * 100 * 100.0) / 100.0);
            stats.put("failureRate", Math.round((double) failedCount / totalCount * 100 * 100.0) / 100.0);
        } else {
            stats.put("completionRate", 0.0);
            stats.put("failureRate", 0.0);
        }
        
        return stats;
    }
    
    /**
     * 매치 기본 정보를 응답에 추가
     */
    public Map<String, Object> addMatchInfo(Map<String, Object> response, String matchId, Long duration, String gameMode) {
        if (matchId != null) {
            response.put("matchId", matchId);
        }
        if (duration != null) {
            response.put("matchDuration", duration);
            response.put("matchDurationMinutes", Math.round(duration / 60.0 * 10.0) / 10.0);
        }
        if (gameMode != null) {
            response.put("gameMode", gameMode);
        }
        
        return response;
    }
    
    /**
     * 플레이어 정보를 응답에 추가
     */
    public Map<String, Object> addPlayerInfo(Map<String, Object> response, String championName, String playerName) {
        if (championName != null) {
            response.put("targetChampion", championName);
        }
        if (playerName != null) {
            response.put("targetPlayerName", playerName);
        }
        
        return response;
    }
}