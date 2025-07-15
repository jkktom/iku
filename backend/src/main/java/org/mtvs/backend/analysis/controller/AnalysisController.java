package org.mtvs.backend.analysis.controller;

import org.mtvs.backend.analysis.entity.MatchAnalysis;
import org.mtvs.backend.analysis.service.MatchAnalysisService;
import org.mtvs.backend.riot.dto.AccountDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RestController - V2로 대체되어 비활성화
// @RequestMapping("/api/analysis")
// @CrossOrigin(origins = "*")
public class AnalysisController {

    private final MatchAnalysisService matchAnalysisService;

    public AnalysisController(MatchAnalysisService matchAnalysisService) {
        this.matchAnalysisService = matchAnalysisService;
    }

    /**
     * 1단계: 초기 분석 레코드 생성
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> createInitialRecord(@RequestBody AccountDto account) {
        try {
            // DB에 초기 레코드 저장
            MatchAnalysis savedRecord = matchAnalysisService.createInitialRecord(account);
            
            // 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("analysisRecord", Map.of(
                "id", savedRecord.getId(),
                "puuid", savedRecord.getPuuid(),
                "status", savedRecord.getAnalysisStatus(),
                "createdAt", savedRecord.getCreatedAt()
            ));
            response.put("message", "분석 레코드 생성 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 2단계: 매치 ID 업데이트
     */
    @PutMapping("/match/{puuid}/{matchId}")
    public ResponseEntity<Map<String, Object>> updateWithMatchId(
            @PathVariable String puuid,
            @PathVariable String matchId) {
        
        try {
            // 매치 ID로 DB 업데이트
            MatchAnalysis updatedRecord = matchAnalysisService.updateWithMatchId(puuid, matchId);
            
            // 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("analysisRecord", Map.of(
                "id", updatedRecord.getId(),
                "puuid", updatedRecord.getPuuid(),
                "matchId", updatedRecord.getMatchId(),
                "status", updatedRecord.getAnalysisStatus(),
                "updatedAt", updatedRecord.getUpdatedAt()
            ));
            response.put("message", "매치 ID 업데이트 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 3단계: 단일 매치 AI 분석 수행
     */
    @PostMapping("/analyze/{puuid}/{matchId}")
    public ResponseEntity<Map<String, Object>> performAIAnalysis(
            @PathVariable String puuid,
            @PathVariable String matchId) {
        
        try {
            // GameAnalysisService를 통한 AI 분석 수행 및 저장
            Map<String, Object> response = matchAnalysisService.performAIAnalysisAndGetResponse(puuid, matchId);
            
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("analysisRecord", response);
            finalResponse.put("message", "AI 분석이 완료되었습니다.");
            
            return ResponseEntity.ok(finalResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 4단계: 다중 매치 AI 분석 수행 (1~5개 매치)
     */
    @PostMapping("/analyze-multiple/{puuid}")
    public ResponseEntity<Map<String, Object>> performMultipleAIAnalysis(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "5") int matchCount) {
        
        try {
            // 입력값 검증
            if (matchCount < 1 || matchCount > 5) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "매치 개수는 1~5개만 가능합니다. 입력값: " + matchCount);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // GameAnalysisService를 통한 다중 매치 AI 분석 수행 및 저장
            Map<String, Object> response = matchAnalysisService.performMultipleAIAnalysisAndGetResponse(puuid, matchCount);
            
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("analysisRecord", response);
            finalResponse.put("message", matchCount + "개 게임 종합 AI 분석이 완료되었습니다.");
            
            return ResponseEntity.ok(finalResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 분석 결과 조회 (특정 사용자의 모든 분석)
     */
    @GetMapping("/user/{puuid}")
    public ResponseEntity<List<MatchAnalysis>> getAnalysisByPuuid(@PathVariable String puuid) {
        List<MatchAnalysis> analyses = matchAnalysisService.getAnalysisByPuuid(puuid);
        return ResponseEntity.ok(analyses);
    }
    
    /**
     * 특정 매치 분석 결과 조회
     */
    @GetMapping("/match/{puuid}/{matchId}")
    public ResponseEntity<MatchAnalysis> getAnalysisByPuuidAndMatchId(
            @PathVariable String puuid, 
            @PathVariable String matchId) {
        return matchAnalysisService.getAnalysisByPuuidAndMatchId(puuid, matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 분석 상태별 조회 (페이징 적용)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MatchAnalysis>> getAnalysisByStatus(
            @PathVariable MatchAnalysis.AnalysisStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MatchAnalysis> analyses = matchAnalysisService.getAnalysisByStatusWithPaging(status, page, size);
        return ResponseEntity.ok(analyses);
    }

    /**
     * 테스트용 - 단순 카운트 조회
     */
    @GetMapping("/test/count")
    public ResponseEntity<Map<String, Object>> getAnalysisCount() {
        try {
            long totalCount = matchAnalysisService.getTotalCount();
            long completedCount = matchAnalysisService.getCompletedCount();
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("completedCount", completedCount);
            result.put("status", "success");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("status", "failed");
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * 처리 대기 중인 분석 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<List<MatchAnalysis>> getPendingAnalysis() {
        List<MatchAnalysis> analyses = matchAnalysisService.getPendingAnalysis();
        return ResponseEntity.ok(analyses);
    }

    /**
     * 실패한 분석 조회
     */
    @GetMapping("/failed")
    public ResponseEntity<List<MatchAnalysis>> getFailedAnalysis() {
        List<MatchAnalysis> analyses = matchAnalysisService.getFailedAnalysis();
        return ResponseEntity.ok(analyses);
    }

    /**
     * 특정 매치의 모든 분석 조회
     */
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<List<MatchAnalysis>> getAnalysisByMatchId(@PathVariable String matchId) {
        List<MatchAnalysis> analyses = matchAnalysisService.getAnalysisByMatchId(matchId);
        return ResponseEntity.ok(analyses);
    }
}
