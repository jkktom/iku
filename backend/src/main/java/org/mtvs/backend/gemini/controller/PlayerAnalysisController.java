package org.mtvs.backend.gemini.controller;

import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/player-analysis")
public class PlayerAnalysisController {
    
    private final GameAnalysisService gameAnalysisService;
    
    public PlayerAnalysisController(GameAnalysisService gameAnalysisService) {
        this.gameAnalysisService = gameAnalysisService;
    }
    
    /**
     * 플레이어의 최근 매치 목록 조회
     */
    @GetMapping("/{gameName}/{tagLine}/matches")
    public ResponseEntity<?> getPlayerMatches(
            @PathVariable String gameName,
            @PathVariable String tagLine,
            @RequestParam(defaultValue = "5") int count) {
        
        try {
            Map<String, Object> result = gameAnalysisService.getPlayerMatches(gameName, tagLine, count);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of(
                    "error", "매치 목록 조회 실패",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                )
            );
        }
    }
    
    /**
     * 특정 매치의 개인 분석 수행
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzePlayerMatch(@RequestBody AnalysisRequest request) {
        
        try {
            String feedback = gameAnalysisService.analyzePlayerMatch(
                request.getGameName(),
                request.getTagLine(), 
                request.getMatchId()
            );
            
            Map<String, Object> response = Map.of(
                "success", true,
                "playerName", request.getGameName(),
                "tagLine", request.getTagLine(),
                "matchId", request.getMatchId(),
                "analysis", feedback,
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of(
                    "success", false,
                    "error", "개인 매치 분석 실패",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                )
            );
        }
    }
    
    /**
     * 분석 요청을 위한 DTO 클래스
     */
    public static class AnalysisRequest {
        private String gameName;
        private String tagLine;
        private String matchId;
        
        public AnalysisRequest() {}
        
        public AnalysisRequest(String gameName, String tagLine, String matchId) {
            this.gameName = gameName;
            this.tagLine = tagLine;
            this.matchId = matchId;
        }
        
        public String getGameName() { return gameName; }
        public void setGameName(String gameName) { this.gameName = gameName; }
        
        public String getTagLine() { return tagLine; }
        public void setTagLine(String tagLine) { this.tagLine = tagLine; }
        
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
    }
}
