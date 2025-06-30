package org.mtvs.backend.riot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.mtvs.backend.gemini.service.GeminiService;
import org.mtvs.backend.riot.Repository.MatchAnalysisRepository;
import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.AIPlayAnalysisDto;
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
        
        // 중복 분석 방지 - matchId가 null이 아닌 경우에만 체크
        if (matchId != null && !matchId.trim().isEmpty()) {
            boolean alreadyExists = matchAnalysisRepository.existsByPuuidAndMatchId(puuid, matchId);
            if (alreadyExists) {
                // 기존 완료된 분석이 있는지 확인
                Optional<MatchAnalysis> existingOpt = matchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
                if (existingOpt.isPresent()) {
                    MatchAnalysis existing = existingOpt.get();
                    if (existing.getAnalysisStatus() == MatchAnalysis.AnalysisStatus.COMPLETED) {
                        logger.info("이미 완료된 분석이 존재함: ID={}", existing.getId());
                        return existing;
                    }
                }
            }
        }
        
        // 해당 레코드 조회 또는 생성
        MatchAnalysis analysis = null;
        
        // 1. puuid와 matchId로 기존 레코드 찾기
        if (matchId != null && !matchId.trim().isEmpty()) {
            Optional<MatchAnalysis> recordOpt = matchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
            if (recordOpt.isPresent()) {
                analysis = recordOpt.get();
                logger.info("기존 레코드 사용: ID={}", analysis.getId());
            }
        }
        
        // 2. 없으면 matchId가 null인 미완료 레코드 찾기
        if (analysis == null) {
            List<MatchAnalysis> incompleteRecords = matchAnalysisRepository.findByPuuidAndMatchIdIsNullOrderByCreatedAtDesc(puuid);
            if (!incompleteRecords.isEmpty()) {
                analysis = incompleteRecords.get(0);
                analysis.setMatchId(matchId); // matchId 업데이트
                logger.info("미완료 레코드에 matchId 추가: ID={}", analysis.getId());
            }
        }
        
        // 3. 그래도 없으면 새 레코드 생성
        if (analysis == null) {
            logger.warn("해당 PUUID의 레코드가 없음. 새 레코드 생성");
            analysis = new MatchAnalysis();
            analysis.setPuuid(puuid);
            analysis.setMatchId(matchId);
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.REQUESTED);
            
            Map<String, Object> data = new HashMap<>();
            data.put("step", "AI_ANALYSIS_DIRECT");
            data.put("puuid", puuid);
            data.put("matchId", matchId);
            data.put("timestamp", System.currentTimeMillis());
            analysis.setAiRequestData(data);
            
            analysis = matchAnalysisRepository.save(analysis);
        }
        
        try {
            // 상태를 PROCESSING으로 변경
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.PROCESSING);
            matchAnalysisRepository.save(analysis);
            
            // GameAnalysisService를 사용하여 AI 분석 수행
            logger.info("GameAnalysisService를 통한 AI 분석 시작...");
            
            // PUUID에서 게임명과 태그 추출 (또는 기존 데이터에서)
            String gameName = analysis.getTargetPlayerName();
            String tagLine = "KR1"; // 기본값 (실제로는 저장된 데이터에서 가져와야 함)
            
            // 기존 요청 데이터에서 게임 정보 추출
            Map<String, Object> requestData = analysis.getAiRequestData();
            if (requestData != null) {
                if (requestData.containsKey("gameName")) {
                    gameName = (String) requestData.get("gameName");
                }
                if (requestData.containsKey("tagLine")) {
                    tagLine = (String) requestData.get("tagLine");
                }
            }
            
            // gameName이 없으면 기본값 설정
            if (gameName == null || gameName.trim().isEmpty()) {
                gameName = "Unknown"; // 기본값
                logger.warn("게임명이 없어서 기본값 사용: {}", gameName);
            }
            
            logger.info("AI 분석 대상: {}#{}, 매치: {}", gameName, tagLine, matchId);
            
            // GameAnalysisService를 통해 상세 분석 수행
            String aiAnalysisResult = gameAnalysisService.analyzePlayerMatch(gameName, tagLine, matchId);
            
            logger.info("AI 분석 완료! 결과 길이: {} 문자", aiAnalysisResult.length());
            
            // AI 응답 데이터 구성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("analysisResult", aiAnalysisResult);
            responseData.put("analysisTimestamp", System.currentTimeMillis());
            responseData.put("analysisMethod", "GameAnalysisService");
            responseData.put("matchId", matchId);
            responseData.put("puuid", puuid);
            
            // 결과 저장
            analysis.setAiResponseData(responseData);
            analysis.setAnalysisSummary(aiAnalysisResult);  // 500자 제한 제거
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.COMPLETED);
            
            // 요청 데이터에 분석 완료 표시 추가
            if (requestData == null) {
                requestData = new HashMap<>();
            }
            requestData.put("step3_aiAnalysis", true);
            requestData.put("analysisCompletedTimestamp", System.currentTimeMillis());
            analysis.setAiRequestData(requestData);
            
            analysis = matchAnalysisRepository.save(analysis);
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
     * 레거시 메서드: 기존 AI 분석 요청 처리 (호환성 유지)
     */
    public MatchAnalysis requestAnalysis(AIPlayAnalysisDto analysisRequest) {
        logger.info("=== 레거시 AI 분석 요청 ===");
        
        String puuid = extractPuuidFromRequest(analysisRequest);
        String matchId = analysisRequest.getMatchId();
        
        // 중복 분석 방지 - matchId가 null이 아닌 경우에만 체크
        if (matchId != null && !matchId.trim().isEmpty()) {
            boolean exists = matchAnalysisRepository.existsByPuuidAndMatchId(puuid, matchId);
            logger.info("중복 분석 체크 - 존재함: {}", exists);
            
            if (exists) {
                logger.warn("중복 분석 요청 거부: PUUID={}, MatchID={}", puuid, matchId);
                throw new IllegalArgumentException("이미 분석된 매치입니다.");
            }
        }

        // 분석 엔티티 생성
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setMatchId(matchId);
        analysis.setTargetPlayerName(analysisRequest.getTargetPlayerName());
        analysis.setTargetChampion(analysisRequest.getTargetChampion());
        analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.REQUESTED);
        
        // 요청 데이터를 JSON으로 저장
        Map<String, Object> requestData = convertToMap(analysisRequest);
        analysis.setAiRequestData(requestData);

        try {
            analysis = matchAnalysisRepository.save(analysis);
            logger.info("레거시 분석 요청 저장 완료! ID: {}", analysis.getId());
            
            // 비동기적으로 AI 분석 수행
            performAnalysisAsync(analysis);
            
        } catch (Exception e) {
            logger.error("레거시 분석 요청 저장 실패: ", e);
            throw new RuntimeException("분석 요청 저장 실패", e);
        }
        
        return analysis;
    }

    /**
     * 비동기 AI 분석 수행 (레거시)
     */
    private void performAnalysisAsync(MatchAnalysis analysis) {
        try {
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.PROCESSING);
            matchAnalysisRepository.save(analysis);
            
            String analysisPrompt = createAnalysisPrompt(analysis);
            String aiResponse = geminiService.sendMessage(analysisPrompt);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("rawResponse", aiResponse);
            responseData.put("timestamp", System.currentTimeMillis());
            responseData.put("model", "gemini");
            
            analysis.setAiResponseData(responseData);
            analysis.setAnalysisSummary(extractSummaryFromResponse(aiResponse));
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.COMPLETED);
            
        } catch (Exception e) {
            logger.error("AI 분석 중 오류 발생: ", e);
            analysis.setAnalysisStatus(MatchAnalysis.AnalysisStatus.FAILED);
            analysis.setErrorMessage(e.getMessage());
        } finally {
            matchAnalysisRepository.save(analysis);
        }
    }

    /**
     * AI 분석을 위한 프롬프트 생성 (레거시)
     */
    private String createAnalysisPrompt(MatchAnalysis analysis) {
        Map<String, Object> requestData = analysis.getAiRequestData();
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 리그 오브 레전드 매치 데이터를 분석해주세요:\n\n");
        prompt.append("매치 ID: ").append(analysis.getMatchId()).append("\n");
        prompt.append("플레이어: ").append(analysis.getTargetPlayerName()).append("\n");
        prompt.append("챔피언: ").append(analysis.getTargetChampion()).append("\n\n");
        
        if (requestData != null) {
            if (requestData.containsKey("playEvents")) {
                prompt.append("주요 플레이 이벤트:\n");
                prompt.append(requestData.get("playEvents").toString()).append("\n\n");
            }
            
            if (requestData.containsKey("matchSummary")) {
                prompt.append("매치 요약:\n");
                prompt.append(requestData.get("matchSummary").toString()).append("\n\n");
            }
        }
        
        prompt.append("이 플레이어의 성능을 분석하고 개선점을 제시해주세요.");
        
        return prompt.toString();
    }

    /**
     * AI 응답에서 요약 추출 (전체 응답 반환)
     */
    private String extractSummaryFromResponse(String aiResponse) {
        return aiResponse;  // 전체 응답 반환
    }

    /**
     * 요청에서 PUUID 추출
     */
    private String extractPuuidFromRequest(AIPlayAnalysisDto request) {
        if (request.getPuuid() != null && !request.getPuuid().trim().isEmpty()) {
            return request.getPuuid();
        }
        
        logger.warn("PUUID가 요청에 포함되지 않음. 임시 값 생성: {}", request.getTargetPlayerName());
        return "temp_puuid_" + (request.getTargetPlayerName() != null ? request.getTargetPlayerName().hashCode() : System.currentTimeMillis());
    }

    /**
     * DTO를 Map으로 변환
     */
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
}
