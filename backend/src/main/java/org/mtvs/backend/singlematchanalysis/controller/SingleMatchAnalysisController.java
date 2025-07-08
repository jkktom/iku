package org.mtvs.backend.singlematchanalysis.controller;

import org.mtvs.backend.analysis.dto.AIAnalysisResponseDto;
import org.mtvs.backend.analysis.entity.MatchAnalysis;
import org.mtvs.backend.singlematchanalysis.service.SingleMatchAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/singleanalysis")
@CrossOrigin(origins = "*")
public class SingleMatchAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(SingleMatchAnalysisController.class);
    
    private final SingleMatchAnalysisService singleMatchAnalysisService;
    
    public SingleMatchAnalysisController(SingleMatchAnalysisService singleMatchAnalysisService) {
        this.singleMatchAnalysisService = singleMatchAnalysisService;
    }
    
    @PostMapping("/create/{puuid}")
    public ResponseEntity<Map<String, Object>> createAnalysisRecord(
            @PathVariable String puuid,
            @RequestParam(required = false) String matchId) {
        try {
            logger.info("Creating single match analysis record for PUUID: {}, Match ID: {}", puuid, matchId);
            
            MatchAnalysis analysis = singleMatchAnalysisService.createInitialRecord(puuid, matchId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysisId", analysis.getId());
            response.put("puuid", analysis.getPuuid());
            response.put("matchId", analysis.getMatchId());
            response.put("status", analysis.getAnalysisStatus());
            response.put("createdAt", analysis.getCreatedAt());
            response.put("message", "Single match analysis record created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating analysis record: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/analyze/{puuid}/{matchId}")
    public ResponseEntity<AIAnalysisResponseDto> performAnalysis(
            @PathVariable String puuid,
            @PathVariable String matchId) {
        try {
            logger.info("Performing single match analysis for PUUID: {}, Match ID: {}", puuid, matchId);
            
            MatchAnalysis analysis = singleMatchAnalysisService.performAnalysis(puuid, matchId);
            AIAnalysisResponseDto response = singleMatchAnalysisService.getAnalysisResponse(analysis.getId());
            
            logger.info("Successfully completed analysis for PUUID: {}, Match ID: {}", puuid, matchId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error performing analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<AIAnalysisResponseDto> analyzeWithRequestBody(
            @RequestBody Map<String, String> requestBody) {
        try {
            String puuid = requestBody.get("puuid");
            String matchId = requestBody.get("matchId");
            
            if (puuid == null || matchId == null) {
                throw new IllegalArgumentException("Both puuid and matchId are required");
            }
            
            logger.info("Performing single match analysis via request body for PUUID: {}, Match ID: {}", puuid, matchId);
            
            MatchAnalysis analysis = singleMatchAnalysisService.performAnalysis(puuid, matchId);
            AIAnalysisResponseDto response = singleMatchAnalysisService.getAnalysisResponse(analysis.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error performing analysis via request body: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{analysisId}")
    public ResponseEntity<AIAnalysisResponseDto> getAnalysis(@PathVariable Long analysisId) {
        try {
            logger.info("Getting single match analysis with ID: {}", analysisId);
            
            AIAnalysisResponseDto response = singleMatchAnalysisService.getAnalysisResponse(analysisId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting analysis: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{puuid}")
    public ResponseEntity<List<MatchAnalysis>> getUseranalysis(@PathVariable String puuid) {
        try {
            logger.info("Getting user analysis for PUUID: {}", puuid);
            
            List<MatchAnalysis> analysis = singleMatchAnalysisService.getUseranalysis(puuid);
            
            logger.info("Found {} analysis for PUUID: {}", analysis.size(), puuid);
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error getting user analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/match/{analysisId}")
    public ResponseEntity<Map<String, Object>> updateMatchId(
            @PathVariable Long analysisId,
            @RequestParam String matchId) {
        try {
            logger.info("Updating match ID for analysis: {}", analysisId);
            
            MatchAnalysis updated = singleMatchAnalysisService.updateMatchId(analysisId, matchId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysisId", updated.getId());
            response.put("matchId", updated.getMatchId());
            response.put("status", updated.getAnalysisStatus());
            response.put("message", "Match ID updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating match ID: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<MatchAnalysis>> getAllSingleMatchAnalyses() {
        try {
            logger.info("Getting all single match analyses");
            
            // For now, get all completed analyses - could be enhanced with pagination
            List<MatchAnalysis> analyses = singleMatchAnalysisService.getAnalysisByStatus("COMPLETED");
            
            logger.info("Found {} single match analyses", analyses.size());
            return ResponseEntity.ok(analyses);
            
        } catch (Exception e) {
            logger.error("Error getting all single match analyses: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}