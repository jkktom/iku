package org.mtvs.backend.multianalysis.controller;

import jakarta.validation.Valid;
import org.mtvs.backend.multianalysis.dto.MultiMatchAnalysisRequest;
import org.mtvs.backend.multianalysis.entity.MultiMatchAnalysis;
import org.mtvs.backend.multianalysis.service.MultiMatchAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/multianalysis")
public class MultiMatchAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiMatchAnalysisController.class);
    
    private final MultiMatchAnalysisService multiMatchAnalysisService;
    
    public MultiMatchAnalysisController(MultiMatchAnalysisService multiMatchAnalysisService) {
        this.multiMatchAnalysisService = multiMatchAnalysisService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<MultiMatchAnalysis> createAnalysisRequest(@Valid @RequestBody MultiMatchAnalysisRequest request) {
        try {
            logger.info("Creating multi-match analysis request for: {}#{}", request.getGameName(), request.getTagLine());
            
            MultiMatchAnalysis analysis = multiMatchAnalysisService.createAnalysisRequest(request);
            
            logger.info("Successfully created analysis request with ID: {}", analysis.getId());
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error creating analysis request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/analyze/{analysisId}")
    public ResponseEntity<MultiMatchAnalysis> performAnalysis(@PathVariable Long analysisId) {
        try {
            logger.info("Performing multi-match analysis for ID: {}", analysisId);
            
            MultiMatchAnalysis analysis = multiMatchAnalysisService.performAnalysis(analysisId);
            
            logger.info("Successfully completed analysis for ID: {}", analysisId);
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error performing analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<MultiMatchAnalysis> createAndAnalyze(@Valid @RequestBody MultiMatchAnalysisRequest request) {
        try {
            logger.info("Creating and analyzing multi-match for: {}#{}", request.getGameName(), request.getTagLine());
            
            MultiMatchAnalysis analysis = multiMatchAnalysisService.createAnalysisRequest(request);
            
            if ("COMPLETED".equals(analysis.getStatus())) {
                logger.info("Analysis already completed, returning existing result");
                return ResponseEntity.ok(analysis);
            }
            
            analysis = multiMatchAnalysisService.performAnalysis(analysis.getId());
            
            logger.info("Successfully created and analyzed for ID: {}", analysis.getId());
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error creating and analyzing: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/history/{puuid}")
    public ResponseEntity<List<MultiMatchAnalysis>> getAnalysisHistory(@PathVariable String puuid) {
        try {
            logger.info("Getting analysis history for PUUID: {}", puuid);
            
            List<MultiMatchAnalysis> history = multiMatchAnalysisService.getAnalysisHistory(puuid);
            
            logger.info("Found {} analysis records for PUUID: {}", history.size(), puuid);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            logger.error("Error getting analysis history: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{analysisId}")
    public ResponseEntity<MultiMatchAnalysis> getAnalysis(@PathVariable Long analysisId) {
        try {
            logger.info("Getting analysis with ID: {}", analysisId);
            
            MultiMatchAnalysis analysis = multiMatchAnalysisService.getAnalysis(analysisId);
            
            logger.info("Successfully retrieved analysis with ID: {}", analysisId);
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error getting analysis: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/completed/{puuid}")
    public ResponseEntity<List<MultiMatchAnalysis>> getCompletedanalysis(@PathVariable String puuid) {
        try {
            logger.info("Getting completed analysis for PUUID: {}", puuid);
            
            List<MultiMatchAnalysis> analysis = multiMatchAnalysisService.getCompletedanalysis(puuid);
            
            logger.info("Found {} completed analysis for PUUID: {}", analysis.size(), puuid);
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error getting completed analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<MultiMatchAnalysis>> getAllMultiMatchAnalyses() {
        try {
            logger.info("Getting all multi match analyses");
            
            // Get all multi match analyses - could be enhanced with pagination and filtering
            List<MultiMatchAnalysis> analyses = multiMatchAnalysisService.getAllAnalyses();
            
            logger.info("Found {} multi match analyses", analyses.size());
            return ResponseEntity.ok(analyses);
            
        } catch (Exception e) {
            logger.error("Error getting all multi match analyses: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}