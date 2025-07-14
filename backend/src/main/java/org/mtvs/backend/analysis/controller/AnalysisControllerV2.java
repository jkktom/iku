package org.mtvs.backend.analysis.controller;

import org.mtvs.backend.analysis.entity.MultipleMatchAnalysis;
import org.mtvs.backend.analysis.entity.SingleMatchAnalysis;
import org.mtvs.backend.analysis.service.MultipleMatchAnalysisService;
import org.mtvs.backend.analysis.service.SingleMatchAnalysisService;
import org.mtvs.backend.riot.dto.AccountDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisControllerV2 {

    private final SingleMatchAnalysisService singleMatchAnalysisService;
    private final MultipleMatchAnalysisService multipleMatchAnalysisService;

    public AnalysisControllerV2(SingleMatchAnalysisService singleMatchAnalysisService,
                               MultipleMatchAnalysisService multipleMatchAnalysisService) {
        this.singleMatchAnalysisService = singleMatchAnalysisService;
        this.multipleMatchAnalysisService = multipleMatchAnalysisService;
    }

    /**
     * 1단계: 단일 매치 분석 초기 레코드 생성
     */
    @PostMapping("/single/init")
    public ResponseEntity<Map<String, Object>> createSingleMatchInitialRecord(
            @RequestBody Map<String, Object> request) {
        try {
            AccountDto account = new AccountDto();
            account.setPuuid((String) request.get("puuid"));
            account.setGameName((String) request.get("gameName"));
            account.setTagLine((String) request.get("tagLine"));
            
            String matchId = (String) request.get("matchId");
            if (matchId == null || matchId.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "단일 매치 분석에는 matchId가 필요합니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            SingleMatchAnalysis savedRecord = singleMatchAnalysisService.createInitialRecord(account, matchId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysisRecord", Map.of(
                "id", savedRecord.getId(),
                "puuid", savedRecord.getPuuid(),
                "matchId", savedRecord.getMatchId(),
                "status", savedRecord.getAnalysisStatus(),
                "createdAt", savedRecord.getCreatedAt()
            ));
            response.put("message", "단일 매치 분석 레코드 생성 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 1단계: 다중 매치 분석 초기 레코드 생성
     */
    @PostMapping("/multiple/init")
    public ResponseEntity<Map<String, Object>> createMultipleMatchInitialRecord(
            @RequestBody Map<String, Object> request) {
        try {
            AccountDto account = new AccountDto();
            account.setPuuid((String) request.get("puuid"));
            account.setGameName((String) request.get("gameName"));
            account.setTagLine((String) request.get("tagLine"));
            
            Integer matchCount = (Integer) request.get("matchCount");
            if (matchCount == null || matchCount < 1 || matchCount > 5) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "매치 개수는 1~5개만 가능합니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            MultipleMatchAnalysis savedRecord = multipleMatchAnalysisService.createInitialRecord(account, matchCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysisRecord", Map.of(
                "id", savedRecord.getId(),
                "puuid", savedRecord.getPuuid(),
                "matchCount", savedRecord.getMatchCount(),
                "status", savedRecord.getAnalysisStatus(),
                "createdAt", savedRecord.getCreatedAt()
            ));
            response.put("message", "다중 매치 분석 레코드 생성 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 2단계: 다중 매치 분석 대상 매치 ID 목록 업데이트
     */
    @PutMapping("/multiple/matches/{puuid}")
    public ResponseEntity<Map<String, Object>> updateMultipleMatchIds(
            @PathVariable String puuid,
            @RequestBody Map<String, Object> request) {
        
        try {
            Integer matchCount = (Integer) request.get("matchCount");
            if (matchCount == null || matchCount < 1 || matchCount > 5) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "매치 개수는 1~5개만 가능합니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            MultipleMatchAnalysis updatedRecord = multipleMatchAnalysisService.updateWithMatchIds(puuid, matchCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analysisRecord", Map.of(
                "id", updatedRecord.getId(),
                "puuid", updatedRecord.getPuuid(),
                "matchCount", updatedRecord.getMatchCount(),
                "analyzedMatchIds", updatedRecord.getAnalyzedMatchIds(),
                "status", updatedRecord.getAnalysisStatus(),
                "updatedAt", updatedRecord.getUpdatedAt()
            ));
            response.put("message", "다중 매치 분석 대상 매치 목록 업데이트 완료");
            
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
    @PostMapping("/single/analyze/{puuid}/{matchId}")
    public ResponseEntity<Map<String, Object>> performSingleAIAnalysis(
            @PathVariable String puuid,
            @PathVariable String matchId) {
        
        try {
            Map<String, Object> response = singleMatchAnalysisService.performAIAnalysisAndGetResponse(puuid, matchId);
            
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("analysisRecord", response);
            finalResponse.put("message", "단일 매치 AI 분석이 완료되었습니다.");
            
            return ResponseEntity.ok(finalResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 3단계: 다중 매치 AI 분석 수행
     */
    @PostMapping("/multiple/analyze/{puuid}")
    public ResponseEntity<Map<String, Object>> performMultipleAIAnalysis(
            @PathVariable String puuid) {
        
        try {
            Map<String, Object> response = multipleMatchAnalysisService.performMultipleAIAnalysisFromExistingRecord(puuid);
            
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("analysisRecord", response);
            finalResponse.put("message", "다중 매치 AI 분석이 완료되었습니다.");
            
            return ResponseEntity.ok(finalResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 단일 매치 분석 결과 조회 (특정 사용자)
     */
    @GetMapping("/single/user/{puuid}")
    public ResponseEntity<List<SingleMatchAnalysis>> getSingleAnalysisByPuuid(@PathVariable String puuid) {
        List<SingleMatchAnalysis> analyses = singleMatchAnalysisService.getAnalysisByPuuid(puuid);
        return ResponseEntity.ok(analyses);
    }

    /**
     * 다중 매치 분석 결과 조회 (특정 사용자)
     */
    @GetMapping("/multiple/user/{puuid}")
    public ResponseEntity<List<MultipleMatchAnalysis>> getMultipleAnalysisByPuuid(@PathVariable String puuid) {
        List<MultipleMatchAnalysis> analyses = multipleMatchAnalysisService.getAnalysisByPuuid(puuid);
        return ResponseEntity.ok(analyses);
    }

    /**
     * 특정 매치의 단일 분석 결과 조회
     */
    @GetMapping("/single/match/{puuid}/{matchId}")
    public ResponseEntity<SingleMatchAnalysis> getSingleAnalysisByPuuidAndMatchId(
            @PathVariable String puuid, 
            @PathVariable String matchId) {
        return singleMatchAnalysisService.getAnalysisByPuuidAndMatchId(puuid, matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 완료된 분석 조회 (통합 - 단일 + 다중)
     */
    @GetMapping("/status/COMPLETED")
    public ResponseEntity<Map<String, Object>> getCompletedAnalysis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            List<SingleMatchAnalysis> singleAnalyses = singleMatchAnalysisService.getAnalysisByStatusWithPaging(
                    SingleMatchAnalysis.AnalysisStatus.COMPLETED, page, size);
            
            List<MultipleMatchAnalysis> multipleAnalyses = multipleMatchAnalysisService.getAnalysisByStatusWithPaging(
                    MultipleMatchAnalysis.AnalysisStatus.COMPLETED, page, size);
            
            // 통합 응답 데이터 구성
            List<Map<String, Object>> combinedResults = new ArrayList<>();
            
            // 단일 분석 결과 변환
            for (SingleMatchAnalysis analysis : singleAnalyses) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", analysis.getId());
                result.put("puuid", analysis.getPuuid());
                result.put("matchId", analysis.getMatchId());
                result.put("targetPlayerName", analysis.getTargetPlayerName());
                result.put("targetChampion", analysis.getTargetChampion());
                result.put("analysisType", "SINGLE");
                result.put("status", analysis.getAnalysisStatus().name());
                result.put("analysisSummary", analysis.getAnalysisSummary());
                result.put("updatedAt", analysis.getUpdatedAt());
                result.put("aiResponseData", analysis.getAiResponseData());
                combinedResults.add(result);
            }
            
            // 다중 분석 결과 변환
            for (MultipleMatchAnalysis analysis : multipleAnalyses) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", analysis.getId());
                result.put("puuid", analysis.getPuuid());
                result.put("matchId", null); // 다중 분석에서는 null
                result.put("targetPlayerName", analysis.getTargetPlayerName());
                result.put("analysisType", "MULTIPLE");
                result.put("matchCount", analysis.getMatchCount());
                result.put("analyzedMatchIds", analysis.getAnalyzedMatchIds());
                result.put("status", analysis.getAnalysisStatus().name());
                result.put("analysisSummary", analysis.getAnalysisSummary());
                result.put("updatedAt", analysis.getUpdatedAt());
                result.put("aiResponseData", analysis.getAiResponseData());
                combinedResults.add(result);
            }
            
            // 최신 순으로 정렬
            combinedResults.sort((a, b) -> {
                Object aDate = a.get("updatedAt");
                Object bDate = b.get("updatedAt");
                if (aDate instanceof java.time.LocalDateTime && bDate instanceof java.time.LocalDateTime) {
                    return ((java.time.LocalDateTime) bDate).compareTo((java.time.LocalDateTime) aDate);
                }
                return 0;
            });
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", combinedResults);
            response.put("totalElements", combinedResults.size());
            response.put("number", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) combinedResults.size() / size));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 통계 정보 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAnalysisStats() {
        try {
            long singleTotalCount = singleMatchAnalysisService.getTotalCount();
            long singleCompletedCount = singleMatchAnalysisService.getCompletedCount();
            long multipleTotalCount = multipleMatchAnalysisService.getTotalCount();
            long multipleCompletedCount = multipleMatchAnalysisService.getCompletedCount();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("singleAnalysis", Map.of(
                "totalCount", singleTotalCount,
                "completedCount", singleCompletedCount
            ));
            stats.put("multipleAnalysis", Map.of(
                "totalCount", multipleTotalCount,
                "completedCount", multipleCompletedCount
            ));
            stats.put("overall", Map.of(
                "totalCount", singleTotalCount + multipleTotalCount,
                "completedCount", singleCompletedCount + multipleCompletedCount
            ));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 처리 대기 중인 분석 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("singleAnalysis", singleMatchAnalysisService.getPendingAnalysis());
        result.put("multipleAnalysis", multipleMatchAnalysisService.getPendingAnalysis());
        return ResponseEntity.ok(result);
    }

    /**
     * 실패한 분석 조회
     */
    @GetMapping("/failed")
    public ResponseEntity<Map<String, Object>> getFailedAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("singleAnalysis", singleMatchAnalysisService.getFailedAnalysis());
        result.put("multipleAnalysis", multipleMatchAnalysisService.getFailedAnalysis());
        return ResponseEntity.ok(result);
    }
}