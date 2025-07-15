package org.mtvs.backend.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.analysis.entity.SingleMatchAnalysis;
import org.mtvs.backend.analysis.repository.SingleMatchAnalysisRepository;
import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.service.RiotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Arrays;

@Service
@Transactional
public class SingleMatchAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(SingleMatchAnalysisService.class);
    
    private final SingleMatchAnalysisRepository singleMatchAnalysisRepository;
    private final GameAnalysisService gameAnalysisService;
    private final RiotService riotService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SingleMatchAnalysisService(SingleMatchAnalysisRepository singleMatchAnalysisRepository,
                                     GameAnalysisService gameAnalysisService,
                                     RiotService riotService,
                                     ObjectMapper objectMapper) {
        this.singleMatchAnalysisRepository = singleMatchAnalysisRepository;
        this.gameAnalysisService = gameAnalysisService;
        this.riotService = riotService;
        this.objectMapper = objectMapper;
    }

    /**
     * 단일 매치 분석 초기 레코드 생성
     */
    public SingleMatchAnalysis createInitialRecord(AccountDto account, String matchId) {
        logger.info("Creating initial single match analysis record for puuid: {} with matchId: {}", account.getPuuid(), matchId);
        
        // 중복 분석 방지
        if (singleMatchAnalysisRepository.existsByPuuidAndMatchId(account.getPuuid(), matchId)) {
            throw new IllegalArgumentException("해당 매치는 이미 분석이 요청되었습니다.");
        }
        
        SingleMatchAnalysis analysis = new SingleMatchAnalysis();
        analysis.setPuuid(account.getPuuid());
        analysis.setMatchId(matchId);
        analysis.setTargetPlayerName(account.getGameName() + "#" + account.getTagLine());
        analysis.setAnalysisStatus(SingleMatchAnalysis.AnalysisStatus.REQUESTED);
        
        return singleMatchAnalysisRepository.save(analysis);
    }


    /**
     * 단일 매치 AI 분석 수행
     */
    public Map<String, Object> performAIAnalysisAndGetResponse(String puuid, String matchId) {
        logger.info("Performing AI analysis for puuid: {}, matchId: {}", puuid, matchId);
        
        SingleMatchAnalysis analysis = singleMatchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId)
                .orElseThrow(() -> new IllegalArgumentException("분석 레코드를 찾을 수 없습니다."));
        
        try {
            analysis.setAnalysisStatus(SingleMatchAnalysis.AnalysisStatus.PROCESSING);
            singleMatchAnalysisRepository.save(analysis);
            
            // Riot API에서 매치 상세 정보 가져오기
            MatchDetailDto matchDetail = riotService.getMatchDetail(matchId);
            MatchTimelineDto matchTimeline = riotService.getMatchTimeline(matchId);
            
            // 매치 정보 추가
            if (matchDetail != null && matchDetail.getInfo() != null) {
                analysis.setMatchDuration(matchDetail.getInfo().getGameDuration());
                analysis.setGameMode(matchDetail.getInfo().getGameMode());
                
                // 타겟 플레이어의 챔피언 정보 찾기
                String targetChampion = matchDetail.getInfo().getParticipants().stream()
                        .filter(p -> p.getPuuid().equals(puuid))
                        .findFirst()
                        .map(p -> p.getChampionName())
                        .orElse(null);
                analysis.setTargetChampion(targetChampion);
            }
            
            // AI 분석 수행
            Map<String, Object> aiRequest = buildAIRequestData(matchDetail, matchTimeline, puuid, matchId);
            analysis.setAiRequestData(aiRequest);
            
            // GameAnalysisService의 analyzePlayerMatch 메서드 사용
            // 플레이어 이름 추출
            String playerName = analysis.getTargetPlayerName();
            String gameName = playerName != null && playerName.contains("#") ? 
                playerName.split("#")[0] : "Unknown";
            String tagLine = playerName != null && playerName.contains("#") ? 
                playerName.split("#")[1] : "Unknown";
            
            String aiResponse = gameAnalysisService.analyzePlayerMatch(gameName, tagLine, matchId);
            
            // AI 응답 데이터 저장
            Map<String, Object> aiResponseData = Map.of(
                    "analysisResult", aiResponse,
                    "analyzedAt", System.currentTimeMillis(),
                    "analysisType", "SINGLE_MATCH"
            );
            
            analysis.setAiResponseData(aiResponseData);
            analysis.setAnalysisSummary(aiResponse);
            analysis.setAnalysisStatus(SingleMatchAnalysis.AnalysisStatus.COMPLETED);
            
            SingleMatchAnalysis savedAnalysis = singleMatchAnalysisRepository.save(analysis);
            
            return buildResponseData(savedAnalysis);
            
        } catch (Exception e) {
            logger.error("AI analysis failed for puuid: {}, matchId: {}", puuid, matchId, e);
            analysis.setAnalysisStatus(SingleMatchAnalysis.AnalysisStatus.FAILED);
            analysis.setErrorMessage(e.getMessage());
            singleMatchAnalysisRepository.save(analysis);
            throw e;
        }
    }

    /**
     * AI 요청 데이터 구성
     */
    private Map<String, Object> buildAIRequestData(MatchDetailDto matchDetail, MatchTimelineDto matchTimeline, String puuid, String matchId) {
        Map<String, Object> requestData = new HashMap<>();
        
        if (matchDetail != null) {
            requestData.put("matchId", matchId); // matchId는 메서드 매개변수에서 가져옴
            requestData.put("gameDuration", matchDetail.getInfo().getGameDuration());
            requestData.put("gameMode", matchDetail.getInfo().getGameMode());
            requestData.put("participantCount", matchDetail.getInfo().getParticipants().size());
        }
        
        if (matchTimeline != null) {
            requestData.put("timelineFrameCount", matchTimeline.getInfo().getFrames().size());
        }
        
        requestData.put("targetPuuid", puuid);
        requestData.put("requestedAt", System.currentTimeMillis());
        
        return requestData;
    }

    /**
     * 응답 데이터 구성
     */
    private Map<String, Object> buildResponseData(SingleMatchAnalysis analysis) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", analysis.getId());
        response.put("puuid", analysis.getPuuid());
        response.put("matchId", analysis.getMatchId());
        response.put("targetPlayerName", analysis.getTargetPlayerName());
        response.put("targetChampion", analysis.getTargetChampion());
        response.put("matchDuration", analysis.getMatchDuration());
        response.put("gameMode", analysis.getGameMode());
        response.put("status", analysis.getAnalysisStatus().name());
        response.put("analysisSummary", analysis.getAnalysisSummary());
        response.put("updatedAt", analysis.getUpdatedAt());
        response.put("aiResponseData", analysis.getAiResponseData());
        
        return response;
    }

    // 조회 메서드들
    public List<SingleMatchAnalysis> getAnalysisByPuuid(String puuid) {
        return singleMatchAnalysisRepository.findByPuuidOrderByCreatedAtDesc(puuid);
    }

    public Optional<SingleMatchAnalysis> getAnalysisByPuuidAndMatchId(String puuid, String matchId) {
        return singleMatchAnalysisRepository.findByPuuidAndMatchId(puuid, matchId);
    }

    public List<SingleMatchAnalysis> getAnalysisByStatus(SingleMatchAnalysis.AnalysisStatus status) {
        return singleMatchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status);
    }

    public List<SingleMatchAnalysis> getAnalysisByStatusWithPaging(SingleMatchAnalysis.AnalysisStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return singleMatchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status, pageable).getContent();
    }

    public List<SingleMatchAnalysis> getAnalysisByMatchId(String matchId) {
        return singleMatchAnalysisRepository.findByMatchIdOrderByCreatedAtDesc(matchId);
    }

    public long getTotalCount() {
        return singleMatchAnalysisRepository.count();
    }

    public long getCompletedCount() {
        return singleMatchAnalysisRepository.countByAnalysisStatus(SingleMatchAnalysis.AnalysisStatus.COMPLETED);
    }

    public List<SingleMatchAnalysis> getPendingAnalysis() {
        return singleMatchAnalysisRepository.findByAnalysisStatusInOrderByCreatedAtAsc(
                Arrays.asList(SingleMatchAnalysis.AnalysisStatus.REQUESTED, SingleMatchAnalysis.AnalysisStatus.PROCESSING));
    }

    public List<SingleMatchAnalysis> getFailedAnalysis() {
        return singleMatchAnalysisRepository.findByAnalysisStatusOrderByUpdatedAtDesc(SingleMatchAnalysis.AnalysisStatus.FAILED);
    }
}