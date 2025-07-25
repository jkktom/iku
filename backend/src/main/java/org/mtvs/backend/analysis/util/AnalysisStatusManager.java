package org.mtvs.backend.analysis.util;

import org.mtvs.backend.analysis.entity.AnalysisStatus;
import org.mtvs.backend.analysis.entity.BaseAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 분석 상태 관리를 담당하는 유틸리티 클래스
 */
@Component
public class AnalysisStatusManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisStatusManager.class);
    
    /**
     * 분석 처리를 시작하고 상태를 PROCESSING으로 변경
     */
    public <T extends BaseAnalysis> T startProcessing(T analysis, JpaRepository<T, Long> repository) {
        logger.info("Starting analysis processing for ID: {}, PUUID: {}", analysis.getId(), analysis.getPuuid());
        analysis.setAnalysisStatus(AnalysisStatus.PROCESSING);
        return repository.save(analysis);
    }
    
    /**
     * 분석을 완료하고 결과를 저장
     */
    public <T extends BaseAnalysis> T completeAnalysis(T analysis, 
                                                      String aiResult, 
                                                      Map<String, Object> responseData, 
                                                      JpaRepository<T, Long> repository) {
        logger.info("Completing analysis for ID: {}, PUUID: {}", analysis.getId(), analysis.getPuuid());
        
        analysis.setAnalysisSummary(aiResult);
        analysis.setAiResponseData(responseData);
        analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
        
        return repository.save(analysis);
    }
    
    /**
     * 분석 실패 처리
     */
    public <T extends BaseAnalysis> T failAnalysis(T analysis, 
                                                  Exception e, 
                                                  JpaRepository<T, Long> repository) {
        logger.error("Analysis failed for ID: {}, PUUID: {}", analysis.getId(), analysis.getPuuid(), e);
        
        analysis.setAnalysisStatus(AnalysisStatus.FAILED);
        analysis.setErrorMessage(e.getMessage());
        
        return repository.save(analysis);
    }
    
    /**
     * 분석 실패 처리 (커스텀 메시지)
     */
    public <T extends BaseAnalysis> T failAnalysis(T analysis, 
                                                  String errorMessage, 
                                                  JpaRepository<T, Long> repository) {
        logger.error("Analysis failed for ID: {}, PUUID: {} - {}", analysis.getId(), analysis.getPuuid(), errorMessage);
        
        analysis.setAnalysisStatus(AnalysisStatus.FAILED);
        analysis.setErrorMessage(errorMessage);
        
        return repository.save(analysis);
    }
    
    /**
     * 분석 상태를 특정 상태로 변경
     */
    public <T extends BaseAnalysis> T updateStatus(T analysis, 
                                                  AnalysisStatus status, 
                                                  JpaRepository<T, Long> repository) {
        logger.info("Updating analysis status to {} for ID: {}, PUUID: {}", 
                   status, analysis.getId(), analysis.getPuuid());
        
        analysis.setAnalysisStatus(status);
        return repository.save(analysis);
    }
    
    /**
     * 분석 재시작 (FAILED 또는 COMPLETED -> REQUESTED)
     */
    public <T extends BaseAnalysis> T restartAnalysis(T analysis, JpaRepository<T, Long> repository) {
        logger.info("Restarting analysis for ID: {}, PUUID: {}", analysis.getId(), analysis.getPuuid());
        
        analysis.setAnalysisStatus(AnalysisStatus.REQUESTED);
        analysis.setErrorMessage(null);
        analysis.setAnalysisSummary(null);
        analysis.setAiResponseData(null);
        
        return repository.save(analysis);
    }
    
    /**
     * 분석 상태가 처리 가능한지 확인
     */
    public boolean canProcess(BaseAnalysis analysis) {
        return analysis.getAnalysisStatus() == AnalysisStatus.REQUESTED;
    }
    
    /**
     * 분석 상태가 재시작 가능한지 확인
     */
    public boolean canRestart(BaseAnalysis analysis) {
        return analysis.getAnalysisStatus() == AnalysisStatus.FAILED || 
               analysis.getAnalysisStatus() == AnalysisStatus.COMPLETED;
    }
    
    /**
     * 분석이 진행중인지 확인
     */
    public boolean isInProgress(BaseAnalysis analysis) {
        return analysis.getAnalysisStatus() == AnalysisStatus.PROCESSING;
    }
    
    /**
     * 분석이 완료되었는지 확인
     */
    public boolean isCompleted(BaseAnalysis analysis) {
        return analysis.getAnalysisStatus() == AnalysisStatus.COMPLETED;
    }
    
    /**
     * 분석이 실패했는지 확인
     */
    public boolean isFailed(BaseAnalysis analysis) {
        return analysis.getAnalysisStatus() == AnalysisStatus.FAILED;
    }
    
    /**
     * 상태별 로그 메시지 생성
     */
    public String getStatusLogMessage(BaseAnalysis analysis) {
        return String.format("Analysis [ID: %d, PUUID: %s, Status: %s, Type: %s]",
                           analysis.getId(),
                           analysis.getPuuid(),
                           analysis.getAnalysisStatus(),
                           analysis.getClass().getSimpleName());
    }
    
    /**
     * 분석 진행률 계산 (전체 대비 완료된 분석 수)
     */
    public double calculateProgress(long completedCount, long totalCount) {
        if (totalCount == 0) return 0.0;
        return Math.round((double) completedCount / totalCount * 100 * 100.0) / 100.0;
    }
}