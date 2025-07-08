package org.mtvs.backend.multianalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.common.service.AiAnalysisService;
import org.mtvs.backend.common.service.RiotApiService;
import org.mtvs.backend.multianalysis.dto.MultiMatchAnalysisRequest;
import org.mtvs.backend.multianalysis.entity.MultiMatchAnalysis;
import org.mtvs.backend.multianalysis.repository.MultiMatchAnalysisRepository;
import org.mtvs.backend.common.constants.AnalysisStatus;
import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class MultiMatchAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiMatchAnalysisService.class);
    
    private final MultiMatchAnalysisRepository repository;
    private final RiotApiService riotApiService;
    private final AiAnalysisService aiAnalysisService;
    private final MultiMatchPromptBuilder promptBuilder;
    private final MatchDataAggregator dataAggregator;
    private final ObjectMapper objectMapper;
    
    public MultiMatchAnalysisService(
            MultiMatchAnalysisRepository repository,
            RiotApiService riotApiService,
            AiAnalysisService aiAnalysisService,
            MultiMatchPromptBuilder promptBuilder,
            MatchDataAggregator dataAggregator,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.riotApiService = riotApiService;
        this.aiAnalysisService = aiAnalysisService;
        this.promptBuilder = promptBuilder;
        this.dataAggregator = dataAggregator;
        this.objectMapper = objectMapper;
    }
    
    public MultiMatchAnalysis createAnalysisRequest(MultiMatchAnalysisRequest request) {
        try {
            AccountDto account = riotApiService.getAccountInfo(request.getGameName(), request.getTagLine());
            
            Optional<MultiMatchAnalysis> existing = repository.findByPuuidAndGameNameAndTagLineAndMatchCount(
                    account.getPuuid(), request.getGameName(), request.getTagLine(), request.getMatchCount());
            
            if (existing.isPresent()) {
                logger.info("Found existing analysis for {}, returning existing record", request.getGameName());
                return existing.get();
            }
            
            MultiMatchAnalysis analysis = new MultiMatchAnalysis(
                    account.getPuuid(), 
                    request.getGameName(), 
                    request.getTagLine(), 
                    request.getMatchCount()
            );
            
            return repository.save(analysis);
            
        } catch (Exception e) {
            logger.error("Error creating analysis request: {}", e.getMessage());
            throw new RuntimeException("Failed to create analysis request: " + e.getMessage());
        }
    }
    
    public MultiMatchAnalysis performAnalysis(Long analysisId) {
        MultiMatchAnalysis analysis = repository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));
        
        if (AnalysisStatus.COMPLETED.equals(analysis.getStatus())) {
            logger.info("Analysis already completed for ID: {}", analysisId);
            return analysis;
        }
        
        try {
            analysis.setStatus(AnalysisStatus.PROCESSING);
            repository.save(analysis);
            
            long startTime = System.currentTimeMillis();
            
            List<String> matchIds = riotApiService.getMatchIds(
                    analysis.getPuuid(), 0, analysis.getMatchCount());
            
            analysis.setMatchIds(String.join(",", matchIds));
            
            List<Map<String, Object>> allMatchData = dataAggregator.aggregateMatchData(
                    matchIds, analysis.getPuuid());
            
            String prompt = promptBuilder.buildAnalysisPrompt(
                    allMatchData, analysis.getGameName(), analysis.getTagLine());
            
            analysis.setAiRequest(prompt);
            
            String aiResponse = aiAnalysisService.analyzeMatchData(prompt);
            
            analysis.setAiResponse(aiResponse);
            analysis.setStatus(AnalysisStatus.COMPLETED);
            analysis.setCompletedAt(LocalDateTime.now());
            analysis.setAnalysisDurationSeconds((System.currentTimeMillis() - startTime) / 1000);
            
            return repository.save(analysis);
            
        } catch (Exception e) {
            logger.error("Error performing analysis: {}", e.getMessage(), e);
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setErrorMessage(e.getMessage());
            return repository.save(analysis);
        }
    }
    
    public List<MultiMatchAnalysis> getAnalysisHistory(String puuid) {
        return repository.findByPuuidOrderByCreatedAtDesc(puuid);
    }
    
    public MultiMatchAnalysis getAnalysis(Long analysisId) {
        return repository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));
    }
    
    public List<MultiMatchAnalysis> getCompletedanalysis(String puuid) {
        return repository.findCompletedanalysisByPuuid(puuid);
    }
    
    public List<MultiMatchAnalysis> getAllAnalyses() {
        return repository.findAll();
    }
}