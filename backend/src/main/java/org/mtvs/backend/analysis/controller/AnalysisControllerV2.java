package org.mtvs.backend.analysis.controller;

import org.mtvs.backend.analysis.entity.AnalysisStatus;
import org.mtvs.backend.analysis.entity.DuoMatchAnalysis;
import org.mtvs.backend.analysis.entity.MultipleMatchAnalysis;
import org.mtvs.backend.analysis.entity.SingleMatchAnalysis;
import org.mtvs.backend.analysis.service.DuoMatchAnalysisService;
import org.mtvs.backend.analysis.service.MultipleMatchAnalysisService;
import org.mtvs.backend.analysis.service.SingleMatchAnalysisService;
import org.mtvs.backend.riot.dto.AccountDto;
import org.springframework.http.HttpStatus;
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
    private final DuoMatchAnalysisService duoMatchAnalysisService;

    public AnalysisControllerV2(SingleMatchAnalysisService singleMatchAnalysisService,
                                MultipleMatchAnalysisService multipleMatchAnalysisService,
                                DuoMatchAnalysisService duoMatchAnalysisService) {
        this.singleMatchAnalysisService = singleMatchAnalysisService;
        this.multipleMatchAnalysisService = multipleMatchAnalysisService;
        this.duoMatchAnalysisService = duoMatchAnalysisService;
    }

    /**
     * 1단계: 단일 매치 분석 초기 레코드 생성
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> createSingleMatchInitialRecord(@RequestBody AccountDto account) {
        try {
            SingleMatchAnalysis savedRecord = singleMatchAnalysisService.createInitialRecord(account);

            Map<String, Object> response = new HashMap<>();
            response.put("analysisRecord", Map.of(
                    "id", savedRecord.getId(),
                    "puuid", savedRecord.getPuuid(),
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
     * 2단계: 다중 매치 분석 대상 매치 ID 목록 업데이트
     */
    @PutMapping("/match/{puuid}/{matchId}")
    public ResponseEntity<Map<String, Object>>updateWithMatchId(
            @PathVariable String puuid,

      @PathVariable String matchId) {

        try {
            SingleMatchAnalysis updatedRecord = singleMatchAnalysisService.updateWithMatchId(puuid, matchId);

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
    @PostMapping("/analyze-multiple/{puuid}")
    public ResponseEntity<Map<String, Object>> performMultipleAIAnalysis(

      @PathVariable String puuid,
            @RequestParam(defaultValue = "5") int matchCount) {

        try {
            if (matchCount < 1 || matchCount > 5) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "매치 개수는 1~5개만 가능합니다. 입력값: " + matchCount);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> response = multipleMatchAnalysisService.performMultipleAIAnalysisAndGetResponse(puuid, matchCount);

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
                    AnalysisStatus.COMPLETED, page, size);

            List<MultipleMatchAnalysis> multipleAnalyses = multipleMatchAnalysisService.getAnalysisByStatusWithPaging(
                    AnalysisStatus.COMPLETED, page, size);

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
                result.put("analysisStatus", analysis.getAnalysisStatus().name());
                result.put("analysisSummary", analysis.getAnalysisSummary());

                // LocalDateTime을 문자열로 변환
                result.put("updatedAt", analysis.getUpdatedAt() != null ? analysis.getUpdatedAt().toString() : "");

                // aiResponseData가 null인 경우 빈 객체로 설정
                result.put("aiResponseData", analysis.getAiResponseData() != null ? analysis.getAiResponseData() : new HashMap<>());

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
                result.put("analysisStatus", analysis.getAnalysisStatus().name());
                result.put("analysisSummary", analysis.getAnalysisSummary());

                // LocalDateTime을 문자열로 변환
                result.put("updatedAt", analysis.getUpdatedAt() != null ? analysis.getUpdatedAt().toString() : "");

                // aiResponseData가 null인 경우 빈 객체로 설정
                result.put("aiResponseData", analysis.getAiResponseData() != null ? analysis.getAiResponseData() : new HashMap<>());
                combinedResults.add(result);
            }

            // 정렬 로직 수정 - null 체크 추가 및 안전한 비교
            combinedResults.sort((a, b) -> {
                String aDate = (String) a.get("updatedAt");
                String bDate = (String) b.get("updatedAt");

                // null 체크
                if (aDate == null && bDate == null) return 0;
                if (aDate == null) return 1;  // null은 마지막에 배치
                if (bDate == null) return -1;

                // 내림차순 정렬 (최신순)
                return bDate.compareTo(aDate);
            });

            // 페이징 처리
            int totalElements = combinedResults.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);

            // 인덱스 범위 체크
            List<Map<String, Object>> pagedResults;
            if (fromIndex < totalElements) {
                pagedResults = combinedResults.subList(fromIndex, toIndex);
            } else {
                pagedResults = new ArrayList<>();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("content", pagedResults);
            response.put("totalElements", combinedResults.size());
            response.put("number", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) combinedResults.size() / size));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            // return ResponseEntity.badRequest().body(errorResponse);

            // 스택 트레이스를 문자열 목록으로 변환
            List<String> stackTraceList = new ArrayList<>();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTraceList.add(element.toString());
            }
            errorResponse.put("stackTrace", stackTraceList);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    /**
     * 공통 매치 찾기
     */
    @GetMapping("/duo/common-matches")
    public ResponseEntity<Map<String, Object>> findCommonMatches(
            @RequestParam String player1Name,
            @RequestParam String player1Tag,
            @RequestParam String player2Name,
            @RequestParam String player2Tag) {
        try {
            List<String> commonMatches = duoMatchAnalysisService.findCommonMatches(
                    player1Name, player1Tag, player2Name, player2Tag);

            Map<String, Object> response = new HashMap<>();
            response.put("commonMatches", commonMatches);
            response.put("count", commonMatches.size());
            response.put("message", "공통 매치 " + commonMatches.size() + "개 발견");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 듀오 비교 분석 수행
     */
    @PostMapping("/duo/analyze")
    public ResponseEntity<Map<String, Object>> performDuoAnalysis(
            @RequestParam String player1Name,
            @RequestParam String player1Tag,
            @RequestParam String player2Name,
            @RequestParam String player2Tag,
            @RequestParam String matchId) {
        try {
            Map<String, Object> response = duoMatchAnalysisService.compareDuoPlayersAndGetResponse(
                    player1Name, player1Tag, player2Name, player2Tag, matchId);

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("analysisRecord", response);
            finalResponse.put("message", "듀오 비교 분석이 완료되었습니다.");

            return ResponseEntity.ok(finalResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 매치 ID로 듀오 분석 조회
     */
    @GetMapping("/duo/match/{matchId}")
    public ResponseEntity<List<DuoMatchAnalysis>> getDuoAnalysisByMatchId(@PathVariable String matchId) {
        List<DuoMatchAnalysis> analyses = duoMatchAnalysisService.getAnalysisByMatchId(matchId);
        return ResponseEntity.ok(analyses);
    }

    /**
     * 듀오 분석 상태별 조회
     */
    @GetMapping("/duo/status/{status}")
    public ResponseEntity<List<DuoMatchAnalysis>> getDuoAnalysisByStatus(
            @PathVariable AnalysisStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<DuoMatchAnalysis> analyses = duoMatchAnalysisService.getAnalysisByStatusWithPaging(status, page, size);
        return ResponseEntity.ok(analyses);
    }

    /**
     * 듀오 분석 통계
     */
    @GetMapping("/duo/stats")
    public ResponseEntity<Map<String, Object>> getDuoAnalysisStats() {
        try {
            long totalCount = duoMatchAnalysisService.getTotalCount();
            long completedCount = duoMatchAnalysisService.getCompletedCount();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", totalCount);
            stats.put("completedCount", completedCount);
            stats.put("pendingCount", totalCount - completedCount);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

}

