package org.mtvs.backend.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.analysis.entity.MultipleMatchAnalysis;
import org.mtvs.backend.analysis.repository.MultipleMatchAnalysisRepository;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class MultipleMatchAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(MultipleMatchAnalysisService.class);
    
    private final MultipleMatchAnalysisRepository multipleMatchAnalysisRepository;
    private final GameAnalysisService gameAnalysisService;
    private final RiotService riotService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MultipleMatchAnalysisService(MultipleMatchAnalysisRepository multipleMatchAnalysisRepository,
                                       GameAnalysisService gameAnalysisService,
                                       RiotService riotService,
                                       ObjectMapper objectMapper) {
        this.multipleMatchAnalysisRepository = multipleMatchAnalysisRepository;
        this.gameAnalysisService = gameAnalysisService;
        this.riotService = riotService;
        this.objectMapper = objectMapper;
    }

    /**
     * 다중 매치 분석 수행
     */
    public Map<String, Object> performMultipleAIAnalysisAndGetResponse(String puuid, int matchCount) {
        logger.info("Performing multiple AI analysis for puuid: {}, matchCount: {}", puuid, matchCount);
        
        try {
            // 매치 ID 목록 가져오기 (최대 20개, 최신부터)
            List<String> matchIds = riotService.getMatchIds(puuid, 0, 20);
            
            if (matchIds.isEmpty()) {
                throw new IllegalArgumentException("해당 플레이어의 매치 기록을 찾을 수 없습니다.");
            }
            
            // 요청된 개수만큼 매치 선택
            List<String> selectedMatchIds = matchIds.stream()
                    .limit(matchCount)
                    .collect(Collectors.toList());
            
            // 초기 레코드 생성
            MultipleMatchAnalysis analysis = new MultipleMatchAnalysis();
            analysis.setPuuid(puuid);
            analysis.setMatchCount(matchCount);
            analysis.setAnalyzedMatchIds(selectedMatchIds);
            analysis.setAnalysisPeriod("최근 " + matchCount + "게임");
            analysis.setTotalGamesFound(matchIds.size());
            analysis.setAnalysisStatus(MultipleMatchAnalysis.AnalysisStatus.PROCESSING);
            
            // 플레이어 이름 설정 (첫 번째 매치에서 가져오기)
            if (!selectedMatchIds.isEmpty()) {
                try {
                    MatchDetailDto firstMatch = riotService.getMatchDetail(selectedMatchIds.get(0));
                    if (firstMatch != null && firstMatch.getInfo() != null) {
                        String playerName = firstMatch.getInfo().getParticipants().stream()
                                .filter(p -> p.getPuuid().equals(puuid))
                                .findFirst()
                                .map(p -> p.getRiotIdGameName() + "#" + p.getRiotIdTagline())
                                .orElse("Unknown Player");
                        analysis.setTargetPlayerName(playerName);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to get player name from first match", e);
                    analysis.setTargetPlayerName("Unknown Player");
                }
            }
            
            MultipleMatchAnalysis savedAnalysis = multipleMatchAnalysisRepository.save(analysis);
            
            // 매치 데이터 수집
            List<MatchDetailDto> matchDetails = new ArrayList<>();
            List<MatchTimelineDto> matchTimelines = new ArrayList<>();
            List<String> successfulMatchIds = new ArrayList<>(); // 성공적으로 가져온 매치 ID들
            
            for (String matchId : selectedMatchIds) {
                try {
                    MatchDetailDto matchDetail = riotService.getMatchDetail(matchId);
                    MatchTimelineDto matchTimeline = riotService.getMatchTimeline(matchId);
                    
                    if (matchDetail != null) {
                        matchDetails.add(matchDetail);
                        successfulMatchIds.add(matchId);
                    }
                    if (matchTimeline != null) {
                        matchTimelines.add(matchTimeline);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to get match data for matchId: {}", matchId, e);
                }
            }
            
            // AI 분석 수행
            Map<String, Object> aiRequest = buildAIRequestData(matchDetails, matchTimelines, puuid, matchCount, successfulMatchIds);
            savedAnalysis.setAiRequestData(aiRequest);
            
            // 플레이어 이름 추출
            String playerName = savedAnalysis.getTargetPlayerName();
            String gameName = playerName != null && playerName.contains("#") ? 
                playerName.split("#")[0] : "Unknown";
            String tagLine = playerName != null && playerName.contains("#") ? 
                playerName.split("#")[1] : "Unknown";
            
            String aiResponse = gameAnalysisService.analyzePlayerMultipleMatches(gameName, tagLine, matchCount);
            
            // AI 응답 데이터 저장
            Map<String, Object> aiResponseData = Map.of(
                    "analysisResult", aiResponse,
                    "analyzedAt", System.currentTimeMillis(),
                    "analysisType", "MULTIPLE_MATCH",
                    "matchCount", matchCount,
                    "analyzedMatchIds", selectedMatchIds
            );
            
            savedAnalysis.setAiResponseData(aiResponseData);
            savedAnalysis.setAnalysisSummary(aiResponse);
            savedAnalysis.setAnalysisStatus(MultipleMatchAnalysis.AnalysisStatus.COMPLETED);
            
            MultipleMatchAnalysis finalAnalysis = multipleMatchAnalysisRepository.save(savedAnalysis);
            
            return buildResponseData(finalAnalysis);
            
        } catch (Exception e) {
            logger.error("Multiple AI analysis failed for puuid: {}, matchCount: {}", puuid, matchCount, e);
            // 실패한 경우 레코드 생성 및 실패 상태 저장
            MultipleMatchAnalysis failedAnalysis = new MultipleMatchAnalysis();
            failedAnalysis.setPuuid(puuid);
            failedAnalysis.setMatchCount(matchCount);
            failedAnalysis.setAnalysisStatus(MultipleMatchAnalysis.AnalysisStatus.FAILED);
            failedAnalysis.setErrorMessage(e.getMessage());
            multipleMatchAnalysisRepository.save(failedAnalysis);
            throw e;
        }
    }

    /**
     * AI 요청 데이터 구성
     */
    private Map<String, Object> buildAIRequestData(List<MatchDetailDto> matchDetails, 
                                                  List<MatchTimelineDto> matchTimelines, 
                                                  String puuid, 
                                                  int matchCount,
                                                  List<String> matchIds) {
        Map<String, Object> requestData = new HashMap<>();
        
        requestData.put("targetPuuid", puuid);
        requestData.put("requestedMatchCount", matchCount);
        requestData.put("actualMatchCount", matchDetails.size());
        requestData.put("requestedAt", System.currentTimeMillis());
        
        // 매치 기본 정보
        List<Map<String, Object>> matchInfos = new ArrayList<>();
        for (int i = 0; i < matchDetails.size() && i < matchIds.size(); i++) {
            MatchDetailDto matchDetail = matchDetails.get(i);
            String matchId = matchIds.get(i);
            
            if (matchDetail != null) {
                Map<String, Object> matchInfo = new HashMap<>();
                matchInfo.put("matchId", matchId); // 매개변수로 받은 matchId 사용
                matchInfo.put("gameDuration", matchDetail.getInfo().getGameDuration());
                matchInfo.put("gameMode", matchDetail.getInfo().getGameMode());
                matchInfo.put("participantCount", matchDetail.getInfo().getParticipants().size());
                matchInfos.add(matchInfo);
            }
        }
        requestData.put("matchInfos", matchInfos);
        
        // 타임라인 정보
        requestData.put("timelineCount", matchTimelines.size());
        
        return requestData;
    }

    /**
     * 응답 데이터 구성
     */
    private Map<String, Object> buildResponseData(MultipleMatchAnalysis analysis) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", analysis.getId());
        response.put("puuid", analysis.getPuuid());
        response.put("matchId", null); // 다중 분석에서는 null
        response.put("targetPlayerName", analysis.getTargetPlayerName());
        response.put("matchCount", analysis.getMatchCount());
        response.put("analyzedMatchIds", analysis.getAnalyzedMatchIds());
        response.put("analysisPeriod", analysis.getAnalysisPeriod());
        response.put("totalGamesFound", analysis.getTotalGamesFound());
        response.put("status", analysis.getAnalysisStatus().name());
        response.put("analysisSummary", analysis.getAnalysisSummary());
        response.put("updatedAt", analysis.getUpdatedAt());
        response.put("aiResponseData", analysis.getAiResponseData());
        
        return response;
    }

    // 조회 메서드들
    public List<MultipleMatchAnalysis> getAnalysisByPuuid(String puuid) {
        return multipleMatchAnalysisRepository.findByPuuidOrderByCreatedAtDesc(puuid);
    }

    public List<MultipleMatchAnalysis> getAnalysisByStatus(MultipleMatchAnalysis.AnalysisStatus status) {
        return multipleMatchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status);
    }

    public List<MultipleMatchAnalysis> getAnalysisByStatusWithPaging(MultipleMatchAnalysis.AnalysisStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return multipleMatchAnalysisRepository.findByAnalysisStatusOrderByCreatedAtDesc(status, pageable).getContent();
    }

    public long getTotalCount() {
        return multipleMatchAnalysisRepository.count();
    }

    public long getCompletedCount() {
        return multipleMatchAnalysisRepository.countByAnalysisStatus(MultipleMatchAnalysis.AnalysisStatus.COMPLETED);
    }

    public List<MultipleMatchAnalysis> getPendingAnalysis() {
        return multipleMatchAnalysisRepository.findByAnalysisStatusInOrderByCreatedAtAsc(
                Arrays.asList(MultipleMatchAnalysis.AnalysisStatus.REQUESTED, MultipleMatchAnalysis.AnalysisStatus.PROCESSING));
    }

    public List<MultipleMatchAnalysis> getFailedAnalysis() {
        return multipleMatchAnalysisRepository.findByAnalysisStatusOrderByUpdatedAtDesc(MultipleMatchAnalysis.AnalysisStatus.FAILED);
    }

    public List<MultipleMatchAnalysis> getAnalysisByMatchId(String matchId) {
        return multipleMatchAnalysisRepository.findByAnalyzedMatchIdsContaining(matchId);
    }
}