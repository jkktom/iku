package org.mtvs.backend.analysis.service;

import org.mtvs.backend.analysis.entity.MatchAnalysis;
import org.mtvs.backend.analysis.repository.MatchAnalysisRepository;
import org.mtvs.backend.common.constants.AnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AnalysisStatusManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisStatusManager.class);
    
    private final MatchAnalysisRepository matchAnalysisRepository;
    
    public AnalysisStatusManager(MatchAnalysisRepository matchAnalysisRepository) {
        this.matchAnalysisRepository = matchAnalysisRepository;
    }
    
    public MatchAnalysis updateStatus(Long analysisId, String status) {
        return updateStatus(analysisId, status, null);
    }
    
    public MatchAnalysis updateStatus(Long analysisId, String status, String errorMessage) {
        try {
            Optional<MatchAnalysis> analysisOpt = matchAnalysisRepository.findById(analysisId);
            if (analysisOpt.isEmpty()) {
                throw new RuntimeException("Analysis not found with ID: " + analysisId);
            }
            
            MatchAnalysis analysis = analysisOpt.get();
            
            logger.info("Updating analysis status from {} to {} for ID: {}", 
                       analysis.getAnalysisStatus(), status, analysisId);
            
            analysis.setAnalysisStatus(status);
            
            if (errorMessage != null) {
                analysis.setErrorMessage(errorMessage);
            }
            
            if (AnalysisStatus.COMPLETED.equals(status)) {
                // Note: MatchAnalysis doesn't have completedAt field, using updatedAt instead
                analysis.setUpdatedAt(LocalDateTime.now());
            }
            
            return matchAnalysisRepository.save(analysis);
            
        } catch (Exception e) {
            logger.error("Error updating analysis status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update analysis status: " + e.getMessage());
        }
    }
    
    public boolean isAnalysisCompleted(Long analysisId) {
        return matchAnalysisRepository.findById(analysisId)
                .map(analysis -> AnalysisStatus.COMPLETED.equals(analysis.getAnalysisStatus()))
                .orElse(false);
    }
    
    public boolean isAnalysisInProgress(Long analysisId) {
        return matchAnalysisRepository.findById(analysisId)
                .map(analysis -> AnalysisStatus.PROCESSING.equals(analysis.getAnalysisStatus()))
                .orElse(false);
    }
    
    public boolean isAnalysisFailed(Long analysisId) {
        return matchAnalysisRepository.findById(analysisId)
                .map(analysis -> AnalysisStatus.FAILED.equals(analysis.getAnalysisStatus()))
                .orElse(false);
    }
    
    public List<MatchAnalysis> getAnalysisByStatus(String status) {
        return matchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status);
    }
    
    public List<MatchAnalysis> getanalysisByPuuidAndStatus(String puuid, String status) {
        return matchAnalysisRepository.findByPuuidAndAnalysisStatusOrderByCreatedAtDesc(puuid, status);
    }
    
    public long getAnalysisCountByStatus(String status) {
        return matchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status).size();
    }
    
    public void markAsCompleted(Long analysisId, String aiResponse, long durationSeconds) {
        try {
            Optional<MatchAnalysis> analysisOpt = matchAnalysisRepository.findById(analysisId);
            if (analysisOpt.isEmpty()) {
                throw new RuntimeException("Analysis not found with ID: " + analysisId);
            }
            
            MatchAnalysis analysis = analysisOpt.get();
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
            analysis.setAnalysisSummary(aiResponse);
            
            matchAnalysisRepository.save(analysis);
            
            logger.info("Successfully marked analysis as completed for ID: {}", analysisId);
            
        } catch (Exception e) {
            logger.error("Error marking analysis as completed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark analysis as completed: " + e.getMessage());
        }
    }
    
    public void markAsFailed(Long analysisId, String errorMessage) {
        try {
            Optional<MatchAnalysis> analysisOpt = matchAnalysisRepository.findById(analysisId);
            if (analysisOpt.isEmpty()) {
                throw new RuntimeException("Analysis not found with ID: " + analysisId);
            }
            
            MatchAnalysis analysis = analysisOpt.get();
            analysis.setAnalysisStatus(AnalysisStatus.FAILED);
            analysis.setErrorMessage(errorMessage);
            
            matchAnalysisRepository.save(analysis);
            
            logger.info("Successfully marked analysis as failed for ID: {}", analysisId);
            
        } catch (Exception e) {
            logger.error("Error marking analysis as failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark analysis as failed: " + e.getMessage());
        }
    }
}