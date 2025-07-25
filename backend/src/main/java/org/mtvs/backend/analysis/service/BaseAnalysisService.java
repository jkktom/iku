package org.mtvs.backend.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.analysis.entity.AnalysisStatus;
import org.mtvs.backend.analysis.entity.BaseAnalysis;
import org.mtvs.backend.analysis.repository.BaseAnalysisRepository;
import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.mtvs.backend.riot.service.RiotService;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 모든 분석 서비스의 공통 기능을 담은 추상 기본 클래스
 * @param <T> 분석 엔티티 타입
 * @param <R> 리포지토리 타입
 */
@Transactional
public abstract class BaseAnalysisService<T extends BaseAnalysis, R extends BaseAnalysisRepository<T>> {
    
    protected final R repository;
    protected final GameAnalysisService gameAnalysisService;
    protected final RiotService riotService;
    protected final ObjectMapper objectMapper;
    protected final Logger logger;
    
    protected BaseAnalysisService(R repository, 
                                GameAnalysisService gameAnalysisService,
                                RiotService riotService, 
                                ObjectMapper objectMapper, 
                                Logger logger) {
        this.repository = repository;
        this.gameAnalysisService = gameAnalysisService;
        this.riotService = riotService;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }
    
    // 공통 조회 메서드들
    public List<T> getAnalysisByPuuid(String puuid) {
        return repository.findByPuuidOrderByCreatedAtDesc(puuid);
    }
    
    public List<T> getAnalysisByStatus(AnalysisStatus status) {
        return repository.findByAnalysisStatusOrderByCreatedAtDesc(status);
    }
    
    public List<T> getAnalysisByStatusWithPaging(AnalysisStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByAnalysisStatusOrderByCreatedAtDesc(status, pageable).getContent();
    }
    
    public long getTotalCount() {
        return repository.count();
    }
    
    public long getCompletedCount() {
        return repository.countByAnalysisStatus(AnalysisStatus.COMPLETED);
    }
    
    public List<T> getPendingAnalysis() {
        return repository.findByAnalysisStatusInOrderByCreatedAtAsc(
                Arrays.asList(AnalysisStatus.REQUESTED, AnalysisStatus.PROCESSING));
    }
    
    public List<T> getFailedAnalysis() {
        return repository.findByAnalysisStatusOrderByUpdatedAtDesc(AnalysisStatus.FAILED);
    }
    
    // 공통 상태 관리 메서드들
    protected T updateStatus(T analysis, AnalysisStatus status) {
        logger.info("Updating analysis status to {} for analysis ID: {}", status, analysis.getId());
        analysis.setAnalysisStatus(status);
        return repository.save(analysis);
    }
    
    protected T startProcessing(T analysis) {
        return updateStatus(analysis, AnalysisStatus.PROCESSING);
    }
    
    protected T completeAnalysis(T analysis, String aiResult, Map<String, Object> responseData) {
        logger.info("Completing analysis for ID: {}", analysis.getId());
        analysis.setAnalysisSummary(aiResult);
        analysis.setAiResponseData(responseData);
        analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
        return repository.save(analysis);
    }
    
    // 공통 에러 처리 메서드
    protected T handleAnalysisError(T analysis, Exception e, String message) {
        logger.error("{} for analysis ID: {}", message, analysis.getId(), e);
        analysis.setAnalysisStatus(AnalysisStatus.FAILED);
        analysis.setErrorMessage(e.getMessage());
        return repository.save(analysis);
    }
    
    // 공통 응답 데이터 빌더
    protected Map<String, Object> buildBaseResponseData(T analysis) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", analysis.getId());
        response.put("puuid", analysis.getPuuid());
        response.put("targetPlayerName", analysis.getTargetPlayerName());
        response.put("status", analysis.getAnalysisStatus().name());
        response.put("analysisSummary", analysis.getAnalysisSummary());
        response.put("createdAt", analysis.getCreatedAt());
        response.put("updatedAt", analysis.getUpdatedAt());
        response.put("aiResponseData", analysis.getAiResponseData());
        
        if (analysis.getErrorMessage() != null) {
            response.put("errorMessage", analysis.getErrorMessage());
        }
        
        return response;
    }
    
    // 공통 AI 분석 요청 데이터 빌더
    protected Map<String, Object> buildBaseAIRequestData(String puuid) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("targetPuuid", puuid);
        requestData.put("requestedAt", System.currentTimeMillis());
        requestData.put("analysisType", getAnalysisType());
        return requestData;
    }
    
    // 공통 AI 응답 데이터 빌더
    protected Map<String, Object> buildAIResponseData(String aiResult) {
        return Map.of(
                "analysisResult", aiResult,
                "analyzedAt", System.currentTimeMillis(),
                "analysisType", getAnalysisType()
        );
    }
    
    // 공통 검증 메서드들
    protected void validatePuuid(String puuid) {
        if (puuid == null || puuid.trim().isEmpty()) {
            throw new IllegalArgumentException("PUUID는 필수입니다.");
        }
        if (puuid.length() != 78) {
            throw new IllegalArgumentException("유효하지 않은 PUUID 형식입니다.");
        }
    }
    
    protected void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
    }
    
    // 템플릿 메서드 패턴 - 하위 클래스에서 구현해야 하는 추상 메서드들
    protected abstract T createNewAnalysis();
    
    protected abstract Map<String, Object> buildSpecificResponseData(T analysis);
    
    protected abstract String getAnalysisType();
    
    protected abstract void validateSpecificRequirements(T analysis);
    
    // 최종 응답 데이터 빌더 (템플릿 메서드)
    public final Map<String, Object> buildResponseData(T analysis) {
        Map<String, Object> baseResponse = buildBaseResponseData(analysis);
        Map<String, Object> specificData = buildSpecificResponseData(analysis);
        
        // 특정 데이터를 기본 응답에 병합
        baseResponse.putAll(specificData);
        
        return baseResponse;
    }
    
    // 공통 분석 실행 템플릿 메서드
    protected final T executeAnalysisTemplate(T analysis, AnalysisExecutor executor) {
        try {
            // 1. 처리 상태로 변경
            analysis = startProcessing(analysis);
            
            // 2. 특정 요구사항 검증
            validateSpecificRequirements(analysis);
            
            // 3. 실제 분석 실행 (하위 클래스에서 구현)
            String aiResult = executor.execute();
            
            // 4. 완료 처리
            Map<String, Object> responseData = buildAIResponseData(aiResult);
            analysis = completeAnalysis(analysis, aiResult, responseData);
            
            return analysis;
            
        } catch (Exception e) {
            return handleAnalysisError(analysis, e, "분석 실행 중 오류 발생");
        }
    }
    
    // 분석 실행을 위한 함수형 인터페이스
    @FunctionalInterface
    protected interface AnalysisExecutor {
        String execute() throws Exception;
    }
}