package org.mtvs.backend.singlematchanalysis.service;

import org.mtvs.backend.analysis.dto.AIAnalysisResponseDto;
import org.mtvs.backend.analysis.entity.MatchAnalysis;
import org.mtvs.backend.analysis.repository.MatchAnalysisRepository;
import org.mtvs.backend.common.service.AiAnalysisService;
import org.mtvs.backend.common.service.MatchDataService;
import org.mtvs.backend.common.service.RiotApiService;
import org.mtvs.backend.common.constants.AnalysisStatus;
import org.mtvs.backend.analysis.service.AnalysisStatusManager;
import org.mtvs.backend.analysis.service.AnalysisResponseBuilder;
import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SingleMatchAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(SingleMatchAnalysisService.class);
    
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final RiotApiService riotApiService;
    private final AiAnalysisService aiAnalysisService;
    private final MatchDataService matchDataService;
    private final AnalysisStatusManager statusManager;
    private final AnalysisResponseBuilder responseBuilder;
    private final GameAnalysisService gameAnalysisService;
    
    public SingleMatchAnalysisService(
            MatchAnalysisRepository matchAnalysisRepository,
            RiotApiService riotApiService,
            AiAnalysisService aiAnalysisService,
            MatchDataService matchDataService,
            AnalysisStatusManager statusManager,
            AnalysisResponseBuilder responseBuilder,
            GameAnalysisService gameAnalysisService) {
        this.matchAnalysisRepository = matchAnalysisRepository;
        this.riotApiService = riotApiService;
        this.aiAnalysisService = aiAnalysisService;
        this.matchDataService = matchDataService;
        this.statusManager = statusManager;
        this.responseBuilder = responseBuilder;
        this.gameAnalysisService = gameAnalysisService;
    }
    
    public MatchAnalysis createInitialRecord(String puuid, String matchId) {
        try {
            logger.info("Creating initial analysis record for PUUID: {}, Match ID: {}", puuid, matchId);
            
            Optional<MatchAnalysis> existing = matchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
            if (existing.isPresent()) {
                logger.info("Analysis record already exists for PUUID: {}, Match ID: {}", puuid, matchId);
                return existing.get();
            }
            
            MatchAnalysis analysis = new MatchAnalysis();
            analysis.setPuuid(puuid);
            analysis.setMatchId(matchId);
            analysis.setAnalysisStatus(AnalysisStatus.REQUESTED);
            
            MatchAnalysis saved = matchAnalysisRepository.save(analysis);
            logger.info("Successfully created analysis record with ID: {}", saved.getId());
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating initial analysis record: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create analysis record: " + e.getMessage());
        }
    }
    
    public MatchAnalysis performAnalysis(String puuid, String matchId) {
        try {
            logger.info("Performing single match analysis for PUUID: {}, Match ID: {}", puuid, matchId);
            
            MatchAnalysis analysis = createInitialRecord(puuid, matchId);
            
            if (AnalysisStatus.COMPLETED.equals(analysis.getAnalysisStatus())) {
                logger.info("Analysis already completed for ID: {}", analysis.getId());
                return analysis;
            }
            
            analysis = statusManager.updateStatus(analysis.getId(), AnalysisStatus.PROCESSING);
            
            long startTime = System.currentTimeMillis();
            
            // Fetch match data
            MatchDetailDto matchDetail = riotApiService.getMatchDetail(matchId);
            MatchTimelineDto matchTimeline = riotApiService.getMatchTimeline(matchId);
            
            // Save match data to database
            matchDataService.saveMatchData(matchId, matchDetail, matchTimeline);
            
            // Generate AI analysis
            String aiResponse = aiAnalysisService.analyzeMatchData("Analysis prompt for match: " + matchId);
            
            // Update analysis with results
            long durationSeconds = (System.currentTimeMillis() - startTime) / 1000;
            statusManager.markAsCompleted(analysis.getId(), aiResponse, durationSeconds);
            
            // Return updated analysis
            return matchAnalysisRepository.findById(analysis.getId())
                    .orElseThrow(() -> new RuntimeException("Analysis not found after completion"));
            
        } catch (Exception e) {
            logger.error("Error performing analysis: {}", e.getMessage(), e);
            
            // Try to mark as failed if we have an analysis record
            try {
                Optional<MatchAnalysis> existing = matchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
                if (existing.isPresent()) {
                    statusManager.markAsFailed(existing.get().getId(), e.getMessage());
                    return existing.get();
                }
            } catch (Exception failedException) {
                logger.error("Error marking analysis as failed: {}", failedException.getMessage());
            }
            
            throw new RuntimeException("Failed to perform analysis: " + e.getMessage());
        }
    }
    
    public AIAnalysisResponseDto getAnalysisResponse(Long analysisId) {
        try {
            Optional<MatchAnalysis> analysisOpt = matchAnalysisRepository.findById(analysisId);
            if (analysisOpt.isEmpty()) {
                throw new RuntimeException("Analysis not found with ID: " + analysisId);
            }
            
            return responseBuilder.buildResponse(analysisOpt.get());
            
        } catch (Exception e) {
            logger.error("Error getting analysis response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get analysis response: " + e.getMessage());
        }
    }
    
    public List<MatchAnalysis> getUseranalysis(String puuid) {
        try {
            return matchAnalysisRepository.findByPuuidOrderByCreatedAtDesc(puuid);
        } catch (Exception e) {
            logger.error("Error getting user analysis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user analysis: " + e.getMessage());
        }
    }
    
    public MatchAnalysis updateMatchId(Long analysisId, String matchId) {
        try {
            Optional<MatchAnalysis> analysisOpt = matchAnalysisRepository.findById(analysisId);
            if (analysisOpt.isEmpty()) {
                throw new RuntimeException("Analysis not found with ID: " + analysisId);
            }
            
            MatchAnalysis analysis = analysisOpt.get();
            analysis.setMatchId(matchId);
            
            return matchAnalysisRepository.save(analysis);
            
        } catch (Exception e) {
            logger.error("Error updating match ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update match ID: " + e.getMessage());
        }
    }
    
    public List<MatchAnalysis> getAnalysisByStatus(String status) {
        try {
            return statusManager.getAnalysisByStatus(status);
        } catch (Exception e) {
            logger.error("Error getting analysis by status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get analysis by status: " + e.getMessage());
        }
    }
}