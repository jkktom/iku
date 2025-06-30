package org.mtvs.backend.riot.controller;

import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.AIPlayAnalysisDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.entity.MatchAnalysis;
import org.mtvs.backend.riot.service.RiotService;
import org.mtvs.backend.riot.service.MatchAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/riot")
@CrossOrigin(origins = "*")
public class RiotController {

    private final RiotService riotService;
    private final MatchAnalysisService matchAnalysisService;

    public RiotController(RiotService riotService, MatchAnalysisService matchAnalysisService) {
        this.riotService = riotService;
        this.matchAnalysisService = matchAnalysisService;
    }

    /**
     * 1단계: PUUID 구하기 + DB 저장
     */
    @GetMapping("/account/{gameName}/{tagLine}")
    public ResponseEntity<Map<String, Object>> getAccountInfo(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        
        try {
            // 1. Riot API로 계정 정보 조회
            AccountDto account = riotService.getAccountInfo(gameName, tagLine);
            
            // 2. DB에 초기 레코드 저장
            MatchAnalysis savedRecord = matchAnalysisService.createInitialRecord(account);
            
            // 3. 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("account", account);
            response.put("analysisRecord", Map.of(
                "id", savedRecord.getId(),
                "puuid", savedRecord.getPuuid(),
                "status", savedRecord.getAnalysisStatus(),
                "createdAt", savedRecord.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 2단계: PUUID로 매치 ID 구하기 + DB 업데이트
     */
    @GetMapping("/matches/{puuid}")
    public ResponseEntity<Map<String, Object>> getMatchIds(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "1") int count) {
        
        try {
            // 1. Riot API로 매치 ID 목록 조회
            List<String> matchIds = riotService.getMatchIds(puuid, start, count);
            
            if (matchIds == null || matchIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("matchIds", matchIds);
                response.put("message", "매치 데이터가 없습니다.");
                return ResponseEntity.ok(response);
            }
            
            // 2. 첫 번째 매치 ID로 DB 업데이트
            String firstMatchId = matchIds.get(0);
            MatchAnalysis updatedRecord = matchAnalysisService.updateWithMatchId(puuid, firstMatchId);
            
            // 3. 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("matchIds", matchIds);
            response.put("selectedMatchId", firstMatchId);
            response.put("analysisRecord", Map.of(
                "id", updatedRecord.getId(),
                "puuid", updatedRecord.getPuuid(),
                "matchId", updatedRecord.getMatchId(),
                "status", updatedRecord.getAnalysisStatus(),
                "updatedAt", updatedRecord.getUpdatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 매치 상세 정보 조회
     */
    @GetMapping("/matches/{matchId}/detail")
    public MatchDetailDto getMatchDetail(@PathVariable String matchId){
        return riotService.getMatchDetail(matchId);
    }
    
    /**
     * 매치 타임라인 조회
     */
    @GetMapping("/matches/{matchId}/timeline")
    public MatchTimelineDto getMatchTimeline(@PathVariable String matchId) {
        return riotService.getMatchTimeline(matchId);
    }

    /**
     * 3단계: AI 분석 수행 + DB 저장 (GameAnalysisService 사용)
     */
    @PostMapping("/analyze/{puuid}/{matchId}")
    public ResponseEntity<Map<String, Object>> performAIAnalysis(
            @PathVariable String puuid,
            @PathVariable String matchId) {
        
        try {
            // GameAnalysisService를 통한 AI 분석 수행 및 저장
            MatchAnalysis analysisResult = matchAnalysisService.performAIAnalysis(puuid, matchId);
            
            // 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("analysisRecord", Map.of(
                "id", analysisResult.getId(),
                "puuid", analysisResult.getPuuid(),
                "matchId", analysisResult.getMatchId(),
                "targetPlayerName", analysisResult.getTargetPlayerName(),
                "status", analysisResult.getAnalysisStatus(),
                "analysisSummary", analysisResult.getAnalysisSummary(),
                "updatedAt", analysisResult.getUpdatedAt()
            ));
            response.put("message", "AI 분석이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 레거시: AI 분석 요청 (기존 방식, 호환성 유지)
     */
    @PostMapping("/analyze")
    public ResponseEntity<MatchAnalysis> analyzeMatch(@RequestBody AIPlayAnalysisDto analysisRequest) {
        try {
            MatchAnalysis result = matchAnalysisService.requestAnalysis(analysisRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 분석 결과 조회
     */
    @GetMapping("/analysis/{puuid}")
    public ResponseEntity<List<MatchAnalysis>> getAnalysisByPuuid(@PathVariable String puuid) {
        List<MatchAnalysis> analyses = matchAnalysisService.getAnalysisByPuuid(puuid);
        return ResponseEntity.ok(analyses);
    }
    
    /**
     * 특정 매치 분석 결과 조회
     */
    @GetMapping("/analysis/{puuid}/{matchId}")
    public ResponseEntity<MatchAnalysis> getAnalysisByPuuidAndMatchId(
            @PathVariable String puuid, 
            @PathVariable String matchId) {
        return matchAnalysisService.getAnalysisByPuuidAndMatchId(puuid, matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 분석 상태별 조회
     */
    @GetMapping("/analysis/status/{status}")
    public ResponseEntity<List<MatchAnalysis>> getAnalysisByStatus(
            @PathVariable MatchAnalysis.AnalysisStatus status) {
        List<MatchAnalysis> analyses = matchAnalysisService.getAnalysisByStatus(status);
        return ResponseEntity.ok(analyses);
    }
}
