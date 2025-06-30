package org.mtvs.backend.riot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.mtvs.backend.gemini.service.GeminiService;
import org.mtvs.backend.riot.Repository.MatchAnalysisRepository;
import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.AIAnalysisResponseDto;

import org.mtvs.backend.riot.entity.MatchAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MatchAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(MatchAnalysisService.class);
    
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final GameAnalysisService gameAnalysisService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MatchAnalysisService(MatchAnalysisRepository matchAnalysisRepository, 
                               GameAnalysisService gameAnalysisService,
                               GeminiService geminiService,
                               ObjectMapper objectMapper) {
        this.matchAnalysisRepository = matchAnalysisRepository;
        this.gameAnalysisService = gameAnalysisService;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    /**
     * 1단계: 계정 조회 시 초기 레코드 생성
     */
    public MatchAnalysis createInitialRecord(AccountDto account) {
        logger.info("=== 1단계: 초기 레코드 생성 ===");
        logger.info("PUUID: {}", account.getPuuid());
        logger.info("게임명: {}", account.getGameName());
        logger.info("태그: {}", account.getTagLine());
        
        // 기존에 matchId가 null인 미완료 레코드가 있는지 확인
        List<MatchAnalysis> incompleteRecords = matchAnalysisRepository.findByPuuidAndMatchIdIsNullOrderByCreatedAtDesc(account.getPuuid());
                
        if (!incompleteRecords.isEmpty()) {
            MatchAnalysis existingRecord = incompleteRecords.get(0);
            logger.info("기존 미완료 레코드 재사용: ID={}", existingRecord.getId());
            
            // 기존 레코드 정보 업데이트
            existingRecord.setTargetPlayerName(account.getGameName());
            Map<String, Object> requestData = existingRecord.getAiRequestData();
            if (requestData == null) {
                requestData = new HashMap<>();
            }
            requestData.put("step1_reused", true);
            requestData.put("reuseTimestamp", System.currentTimeMillis());
            existingRecord.setAiRequestData(requestData);
            
            return matchAnalysisRepository.save(existingRecord);
        }
        
        // 새 레코드 생성
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(account.getPuuid());
        analysis.setTargetPlayerName(account.getGameName());
        analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.REQUESTED);
        // matchId는 의도적으로 null로 설정 (2단계에서 설정됨)
        
        // 계정 정보를 요청 데이터로 저장
        Map<String, Object> accountData = new HashMap<>();
        accountData.put("step", "ACCOUNT_LOOKUP");
        accountData.put("puuid", account.getPuuid());
        accountData.put("gameName", account.getGameName());
        accountData.put("tagLine", account.getTagLine());
        accountData.put("timestamp", System.currentTimeMillis());
        
        analysis.setAiRequestData(accountData);
        
        try {
            analysis = matchAnalysisRepository.save(analysis);
            logger.info("초기 레코드 생성 완료! ID: {}", analysis.getId());
        } catch (Exception e) {
            logger.error("초기 레코드 생성 실패: ", e);
            throw new RuntimeException("초기 레코드 생성 실패", e);
        }
        
        return analysis;
    }

    /**
     * 2단계: 매치 ID 저장
     */
    public MatchAnalysis updateWithMatchId(String puuid, String matchId) {
        logger.info("=== 2단계: 매치 ID 업데이트 ===");
        logger.info("PUUID: {}", puuid);
        logger.info("매치 ID: {}", matchId);
        
        // 해당 PUUID의 최신 레코드 조회
        List<MatchAnalysis> records = matchAnalysisRepository.findByPuuidOrderByCreatedAtDesc(puuid);
        
        if (records.isEmpty()) {
            logger.warn("해당 PUUID의 레코드가 없음. 새 레코드 생성");
            MatchAnalysis newRecord = new MatchAnalysis();
            newRecord.setPuuid(puuid);
            newRecord.setMatchId(matchId);
            newRecord.setAnalysisStatus(MatchAnalysis.AnalysisStatus.REQUESTED);
            
            Map<String, Object> data = new HashMap<>();
            data.put("step", "MATCH_LOOKUP");
            data.put("puuid", puuid);
            data.put("matchId", matchId);
            data.put("timestamp", System.currentTimeMillis());
            newRecord.setAiRequestData(data);
            
            return matchAnalysisRepository.save(newRecord);
        }
        
        // 최신 레코드 업데이트
        MatchAnalysis analysis = records.get(0);
        analysis.setMatchId(matchId);
        
        // 기존 요청 데이터에 매치 정보 추가
        Map<String, Object> requestData = analysis.getAiRequestData();
        if (requestData == null) {
            requestData = new HashMap<>();
        }
        
        requestData.put("step2_matchLookup", true);
        requestData.put("matchId", matchId);
        requestData.put("matchLookupTimestamp", System.currentTimeMillis());
        
        analysis.setAiRequestData(requestData);
        
        try {
            analysis = matchAnalysisRepository.save(analysis);
            logger.info("매치 ID 업데이트 완료! 레코드 ID: {}", analysis.getId());
        } catch (Exception e) {
            logger.error("매치 ID 업데이트 실패: ", e);
            throw new RuntimeException("매치 ID 업데이트 실패", e);
        }
        
        return analysis;
    }

    /**
     * 3단계: AI 분석 수행 및 응답 저장 (GameAnalysisService 사용)
     */
    public MatchAnalysis performAIAnalysis(String puuid, String matchId) {
        logger.info("=== 3단계: AI 분석 수행 ===");
        logger.info("PUUID: {}", puuid);
        logger.info("매치 ID: {}", matchId);
        
        // 입력값 검증
        if (puuid == null || puuid.trim().isEmpty()) {
            throw new IllegalArgumentException("PUUID는 필수입니다.");
        }
        if (matchId == null || matchId.trim().isEmpty()) {
            throw new IllegalArgumentException("매치 ID는 필수입니다.");
        }
        
        // 중복 분석 방지
        Optional<MatchAnalysis> existingOpt = matchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
        if (existingOpt.isPresent()) {
            MatchAnalysis existing = existingOpt.get();
            if (existing.getAnalysisStatus() == MatchAnalysis.AnalysisStatus.COMPLETED) {
                logger.info("이미 완료된 분석이 존재함: ID={}", existing.getId());
                return existing;
            }
        }
        
        // 해당 레코드 조회 또는 생성
        MatchAnalysis analysis = findOrCreateAnalysisRecord(puuid, matchId);
        
        try {
            // 상태를 PROCESSING으로 변경
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.PROCESSING);
            matchAnalysisRepository.save(analysis);
            
            // AI 분석 수행
            String aiAnalysisResult = performGameAnalysis(analysis, puuid, matchId);
            
            // 결과 저장
            saveAnalysisResult(analysis, aiAnalysisResult, puuid, matchId);
            
            logger.info("AI 분석 결과 저장 완료! 레코드 ID: {}", analysis.getId());
            
        } catch (Exception e) {
            logger.error("AI 분석 실패: ", e);
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.FAILED);
            analysis.setErrorMessage("AI 분석 실패: " + e.getMessage());
            matchAnalysisRepository.save(analysis);
            throw new RuntimeException("AI 분석 실패", e);
        }
        
        return analysis;
    }

    /**
     * 분석 레코드 조회 또는 생성
     */
    private MatchAnalysis findOrCreateAnalysisRecord(String puuid, String matchId) {
        // 1. puuid와 matchId로 기존 레코드 찾기
        Optional<MatchAnalysis> recordOpt = matchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
        if (recordOpt.isPresent()) {
            logger.info("기존 레코드 사용: ID={}", recordOpt.get().getId());
            return recordOpt.get();
        }
        
        // 2. matchId가 null인 미완료 레코드 찾기
        List<MatchAnalysis> incompleteRecords = matchAnalysisRepository.findByPuuidAndMatchIdIsNullOrderByCreatedAtDesc(puuid);
        if (!incompleteRecords.isEmpty()) {
            MatchAnalysis analysis = incompleteRecords.get(0);
            analysis.setMatchId(matchId);
            logger.info("미완료 레코드에 matchId 추가: ID={}", analysis.getId());
            return analysis;
        }
        
        // 3. 새 레코드 생성
        logger.warn("해당 PUUID의 레코드가 없음. 새 레코드 생성");
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setMatchId(matchId);
        analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.REQUESTED);
        
        Map<String, Object> data = new HashMap<>();
        data.put("step", "AI_ANALYSIS_DIRECT");
        data.put("puuid", puuid);
        data.put("matchId", matchId);
        data.put("timestamp", System.currentTimeMillis());
        analysis.setAiRequestData(data);
        
        return matchAnalysisRepository.save(analysis);
    }

    /**
     * 게임 분석 수행
     */
    private String performGameAnalysis(MatchAnalysis analysis, String puuid, String matchId) {
        logger.info("GameAnalysisService를 통한 AI 분석 시작...");
        
        // 게임명과 태그 추출
        String gameName = extractGameName(analysis);
        String tagLine = extractTagLine(analysis);
        
        logger.info("AI 분석 대상: {}#{}, 매치: {}", gameName, tagLine, matchId);
        
        // GameAnalysisService를 통해 상세 분석 수행
        String aiAnalysisResult = gameAnalysisService.analyzePlayerMatch(gameName, tagLine, matchId);
        
        logger.info("AI 분석 완료! 결과 길이: {} 문자", aiAnalysisResult.length());
        
        return aiAnalysisResult;
    }

    /**
     * 분석 결과 저장
     */
    private void saveAnalysisResult(MatchAnalysis analysis, String aiAnalysisResult, String puuid, String matchId) {
        // AI 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("analysisResult", aiAnalysisResult);
        responseData.put("analysisTimestamp", System.currentTimeMillis());
        responseData.put("analysisMethod", "GameAnalysisService");
        responseData.put("matchId", matchId);
        responseData.put("puuid", puuid);
        
        // 결과 저장
        analysis.setAiResponseData(responseData);
        analysis.setAnalysisSummary(aiAnalysisResult);
        analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.COMPLETED);
        
        // 요청 데이터에 분석 완료 표시 추가
        Map<String, Object> requestData = analysis.getAiRequestData();
        if (requestData == null) {
            requestData = new HashMap<>();
        }
        requestData.put("step3_aiAnalysis", true);
        requestData.put("analysisCompletedTimestamp", System.currentTimeMillis());
        analysis.setAiRequestData(requestData);
        
        matchAnalysisRepository.save(analysis);
    }

    /**
     * 게임명 추출
     */
    private String extractGameName(MatchAnalysis analysis) {
        String gameName = analysis.getTargetPlayerName();
        
        // 기존 요청 데이터에서 게임 정보 추출
        Map<String, Object> requestData = analysis.getAiRequestData();
        if (requestData != null && requestData.containsKey("gameName")) {
            gameName = (String) requestData.get("gameName");
        }
        
        // gameName이 없으면 기본값 설정
        if (gameName == null || gameName.trim().isEmpty()) {
            gameName = "Unknown";
            logger.warn("게임명이 없어서 기본값 사용: {}", gameName);
        }
        
        return gameName;
    }

    /**
     * 태그라인 추출
     */
    private String extractTagLine(MatchAnalysis analysis) {
        String tagLine = "KR1"; // 기본값
        
        // 기존 요청 데이터에서 태그 정보 추출
        Map<String, Object> requestData = analysis.getAiRequestData();
        if (requestData != null && requestData.containsKey("tagLine")) {
            tagLine = (String) requestData.get("tagLine");
        }
        
        return tagLine;
    }

//    /**
//     * 레거시 메서드: 기존 AI 분석 요청 처리 (사용 중단됨)
//     * 현재는 performAIAnalysis 메소드를 사용하세요.
//     */
//    @Deprecated
//    public MatchAnalysis requestAnalysis(AIPlayAnalysisDto analysisRequest) {
//        logger.warn("=== 레거시 AI 분석 요청 (Deprecated) ===");
//        logger.warn("이 메소드는 사용 중단되었습니다. performAIAnalysis를 사용하세요.");
//
//        // 기존 로직을 간소화하여 새로운 방식으로 처리
//        String puuid = extractPuuidFromRequest(analysisRequest);
//        String matchId = analysisRequest.getMatchId();
//
//        return performAIAnalysis(puuid, matchId);
//    }
//
//    /**
//     * 요청에서 PUUID 추출 (레거시 지원용)
//     */
//    @Deprecated
//    private String extractPuuidFromRequest(AIPlayAnalysisDto request) {
//        if (request.getPuuid() != null && !request.getPuuid().trim().isEmpty()) {
//            return request.getPuuid();
//        }
//
//        logger.warn("PUUID가 요청에 포함되지 않음. 임시 값 생성: {}", request.getTargetPlayerName());
//        return "temp_puuid_" + (request.getTargetPlayerName() != null ? request.getTargetPlayerName().hashCode() : System.currentTimeMillis());
//    }

    /**
     * DTO를 Map으로 변환 (레거시 지원용)
     */
    @Deprecated
    private Map<String, Object> convertToMap(Object obj) {
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            logger.error("객체를 Map으로 변환 중 오류: ", e);
            return new HashMap<>();
        }
    }

    // 조회 메서드들
    @Transactional(readOnly = true)
    public List<MatchAnalysis> getAnalysisByPuuid(String puuid) {
        return matchAnalysisRepository.findByPuuidOrderByCreatedAtDesc(puuid);
    }

    @Transactional(readOnly = true)
    public Optional<MatchAnalysis> getAnalysisByPuuidAndMatchId(String puuid, String matchId) {
        return matchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
    }

    @Transactional(readOnly = true)
    public List<MatchAnalysis> getAnalysisByStatus(MatchAnalysis.AnalysisStatus status) {
        return matchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<MatchAnalysis> getAnalysisByMatchId(String matchId) {
        return matchAnalysisRepository.findByMatchIdOrderByCreatedAtDesc(matchId);
    }

    @Transactional(readOnly = true)
    public List<MatchAnalysis> getPendingAnalysis() {
        return matchAnalysisRepository.findPendingAnalysis();
    }

    @Transactional(readOnly = true)
    public List<MatchAnalysis> getFailedAnalysis() {
        return matchAnalysisRepository.findFailedAnalysis();
    }

    /**
     * MatchAnalysis Entity를 AIAnalysisResponseDto로 변환
     */
    public AIAnalysisResponseDto convertToResponseDto(MatchAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        
        AIAnalysisResponseDto dto = new AIAnalysisResponseDto();
        dto.setAnalysisId(analysis.getId());
        dto.setPuuid(analysis.getPuuid());
        dto.setMatchId(analysis.getMatchId());
        dto.setTargetPlayerName(analysis.getTargetPlayerName());
        dto.setTargetChampion(analysis.getTargetChampion());
        dto.setAnalysisStatus(analysis.getAnalysisStatus().toString());
        dto.setAnalysisSummary(analysis.getAnalysisSummary());
        dto.setCreatedAt(analysis.getCreatedAt());
        dto.setUpdatedAt(analysis.getUpdatedAt());
        dto.setErrorMessage(analysis.getErrorMessage());
        
        return dto;
    }

    /**
     * AI 응답을 구조화된 형태로 파싱 (향후 확장 가능)
     */
    private AIAnalysisResponseDto parseAIResponse(String aiResponse, MatchAnalysis analysis) {
        AIAnalysisResponseDto dto = convertToResponseDto(analysis);
        
        // 현재는 단순하게 전체 응답을 summary로 저장
        // 향후 AI 응답을 파싱해서 구조화된 데이터로 변환 가능
        dto.setAnalysisSummary(aiResponse);
        
        // 예시: AI 응답에서 특정 섹션 추출 (향후 구현)
        // if (aiResponse.contains("## 성능 분석")) {
        //     AIAnalysisResponseDto.PerformanceAnalysis performance = parsePerformanceSection(aiResponse);
        //     dto.setPerformanceAnalysis(performance);
        // }
        
        return dto;
    }

    /**
     * 분석 결과를 컨트롤러 응답용 Map으로 변환
     */
    public Map<String, Object> createAnalysisResponseMap(MatchAnalysis analysis) {
        if (analysis == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", analysis.getId());
        response.put("puuid", analysis.getPuuid());
        response.put("matchId", analysis.getMatchId());
        response.put("targetPlayerName", analysis.getTargetPlayerName());
        response.put("status", analysis.getAnalysisStatus());
        response.put("analysisSummary", analysis.getAnalysisSummary());
        response.put("aiResponseData", analysis.getAiResponseData());
        response.put("updatedAt", analysis.getUpdatedAt());
        response.put("createdAt", analysis.getCreatedAt());
        
        // 에러가 있는 경우에만 에러 메시지 포함
        if (analysis.getErrorMessage() != null && !analysis.getErrorMessage().trim().isEmpty()) {
            response.put("errorMessage", analysis.getErrorMessage());
        }
        
        return response;
    }

    /**
     * AI 분석 수행 후 컨트롤러 응답용 Map 반환 (편의 메소드)
     */
    public Map<String, Object> performAIAnalysisAndGetResponse(String puuid, String matchId) {
        MatchAnalysis result = performAIAnalysis(puuid, matchId);
        return createAnalysisResponseMap(result);
    }
}
