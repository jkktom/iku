package org.mtvs.backend.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtvs.backend.gemini.service.GameAnalysisService;
import org.mtvs.backend.gemini.service.GeminiService;
import org.mtvs.backend.analysis.repository.MatchAnalysisRepository;
import org.mtvs.backend.common.constants.AnalysisStatus;
import org.mtvs.backend.riot.Repository.*;
import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.analysis.dto.AIAnalysisResponseDto;

import org.mtvs.backend.analysis.entity.MatchAnalysis;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.entity.*;
import org.mtvs.backend.common.constants.EventType;
import org.mtvs.backend.riot.service.RiotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.mtvs.backend.riot.dto.*;
import org.mtvs.backend.riot.dto.InfoDto;

import org.mtvs.backend.riot.Repository.MatchRepository;
import org.mtvs.backend.riot.Repository.ParticipantRepository;
import org.mtvs.backend.riot.Repository.MatchTimelineRepository;
import org.mtvs.backend.riot.Repository.ParticipantFrameRepository;
import org.mtvs.backend.riot.Repository.MatchEventRepository;

@Service
@Transactional
public class MatchAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(MatchAnalysisService.class);
    
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final GameAnalysisService gameAnalysisService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final RiotUserRepository riotUserRepository;
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final MatchTimelineRepository matchTimelineRepository;
    private final ParticipantFrameRepository participantFrameRepository;
    private final MatchEventRepository matchEventRepository;
    private final RiotService riotService;

    @Autowired
    public MatchAnalysisService(MatchAnalysisRepository matchAnalysisRepository,
                                GameAnalysisService gameAnalysisService,
                                GeminiService geminiService,
                                ObjectMapper objectMapper,
                                RiotUserRepository riotUserRepository, MatchRepository matchRepository, ParticipantRepository participantRepository, MatchTimelineRepository matchTimelineRepository, ParticipantFrameRepository participantFrameRepository, MatchEventRepository matchEventRepository, RiotService riotService) {
        this.matchAnalysisRepository = matchAnalysisRepository;
        this.gameAnalysisService = gameAnalysisService;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
        this.riotUserRepository = riotUserRepository;
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.matchTimelineRepository = matchTimelineRepository;
        this.participantFrameRepository = participantFrameRepository;
        this.matchEventRepository = matchEventRepository;
        this.riotService = riotService;
    }

    /**
     * MatchDetailDto를 Map으로 변환
     */
    private Map<String, Object> convertMatchDetailToMap(MatchDetailDto matchDetail) {
        Map<String, Object> matchData = new HashMap<>();

        if (matchDetail.getInfo() != null) {
            InfoDto info = matchDetail.getInfo();

            matchData.put("gameDuration", info.getGameDuration());
            matchData.put("gameMode", info.getGameMode());
            matchData.put("gameVersion", info.getGameVersion());
            matchData.put("queueId", info.getQueueId());

            // participants 변환
            List<Map<String, Object>> participants = new ArrayList<>();
            if (info.getParticipants() != null) {
                for (ParticipantDto participant : info.getParticipants()) {
                    Map<String, Object> participantMap = new HashMap<>();
                    participantMap.put("participantId", participant.getParticipantId());
                    participantMap.put("puuid", participant.getPuuid());
                    participantMap.put("riotIdGameName", participant.getRiotIdGameName());
                    participantMap.put("riotIdTagline", participant.getRiotIdTagline());
                    participantMap.put("summonerName", participant.getSummonerName());
                    participantMap.put("championName", participant.getChampionName());
                    participantMap.put("kills", participant.getKills());
                    participantMap.put("deaths", participant.getDeaths());
                    participantMap.put("assists", participant.getAssists());
                    participantMap.put("totalDamageDealtToChampions", participant.getTotalDamageDealtToChampions());
                    participantMap.put("totalDamageTaken", participant.getTotalDamageTaken());
                    participantMap.put("visionScore", participant.getVisionScore());
                    participantMap.put("goldEarned", participant.getGoldEarned());
                    participantMap.put("totalMinionsKilled", participant.getTotalMinionsKilled());
                    participantMap.put("neutralMinionsKilled", participant.getNeutralMinionsKilled());
                    participantMap.put("teamId", participant.getTeamId());
                    participantMap.put("win", participant.isWin());

                    participants.add(participantMap);
                }
            }
            matchData.put("participants", participants);
        }

        return matchData;
    }

    /**
     * MatchTimelineDto를 Map으로 변환
     */
    private Map<String, Object> convertMatchTimelineToMap(MatchTimelineDto matchTimeline) {
        Map<String, Object> timelineData = new HashMap<>();

        if (matchTimeline.getInfo() != null && matchTimeline.getInfo().getFrames() != null) {
            List<Map<String, Object>> frames = new ArrayList<>();

            for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
                Map<String, Object> frameMap = new HashMap<>();
                frameMap.put("timestamp", frame.getTimestamp());

                // participantFrames 변환
                Map<String, Object> participantFrames = new HashMap<>();
                if (frame.getParticipantFrames() != null) {
                    for (Map.Entry<String, ParticipantFrameDto> entry : frame.getParticipantFrames().entrySet()) {
                        ParticipantFrameDto participantFrame = entry.getValue();
                        Map<String, Object> frameInfo = new HashMap<>();

                        // position 정보
                        Map<String, Object> position = new HashMap<>();
                        position.put("x", participantFrame.getPosition().getX());
                        position.put("y", participantFrame.getPosition().getY());
                        frameInfo.put("position", position);

                        // 기타 정보들
                        frameInfo.put("totalGold", participantFrame.getTotalGold());
                        frameInfo.put("level", participantFrame.getLevel());
                        frameInfo.put("minionsKilled", participantFrame.getMinionsKilled());
                        frameInfo.put("jungleMinionsKilled", participantFrame.getJungleMinionsKilled());

                        participantFrames.put(entry.getKey(), frameInfo);
                    }
                }
                frameMap.put("participantFrames", participantFrames);

                // events 변환
                List<Map<String, Object>> events = new ArrayList<>();
                if (frame.getEvents() != null) {
                    for (EventDto event : frame.getEvents()) {
                        Map<String, Object> eventMap = new HashMap<>();
                        eventMap.put("type", event.getType());
                        eventMap.put("timestamp", event.getTimestamp());
                        eventMap.put("participantId", event.getParticipantId());
                        eventMap.put("killerId", event.getKillerId());
                        eventMap.put("victimId", event.getVictimId());
                        eventMap.put("assistingParticipantIds", event.getAssistingParticipantIds());
                        eventMap.put("monsterType", event.getMonsterType());
                        eventMap.put("buildingType", event.getBuildingType());
                        eventMap.put("laneType", event.getLaneType());
                        eventMap.put("towerType", event.getTowerType());
                        eventMap.put("itemId", event.getItemId());

                        events.add(eventMap);
                    }
                }
                frameMap.put("events", events);

                frames.add(frameMap);
            }

            timelineData.put("frames", frames);
        }

        return timelineData;
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
        analysis.setAnalysisStatus(AnalysisStatus.REQUESTED);
        // matchId는 의도적으로 null로 설정 (2단계에서 설정됨)

        //RiotUser 엔티티 저장
        if(!riotUserRepository.existsByPuuid(account.getPuuid())){
            RiotUser riotUser = new RiotUser();
            riotUser.setPuuid(account.getPuuid());
            riotUser.setGameName(account.getGameName());
            riotUser.setTagLine(account.getTagLine());
            riotUserRepository.save(riotUser);
            logger.info("Riotuser 저장 완료: {}", account.getPuuid());
        }

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
            newRecord.setAnalysisStatus(AnalysisStatus.REQUESTED);
            
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
     * 다중 매치 AI 분석 수행 (1~5개 매치)
     */
    public MatchAnalysis performMultipleAIAnalysis(String puuid, int matchCount) {
        logger.info("=== 다중 매치 AI 분석 수행 ===");
        logger.info("PUUID: {}", puuid);
        logger.info("매치 개수: {}", matchCount);
        
        // 입력값 검증
        if (puuid == null || puuid.trim().isEmpty()) {
            throw new IllegalArgumentException("PUUID는 필수입니다.");
        }
        if (matchCount < 1 || matchCount > 5) {
            throw new IllegalArgumentException("매치 개수는 1~5개만 가능합니다. 입력값: " + matchCount);
        }
        
        // 게임명과 태그 추출 (기존 데이터에서)
        String gameName = "Unknown";
        String tagLine = "KR1";
        
        // 최신 레코드에서 게임명/태그 추출 시도
        List<MatchAnalysis> recentRecords = matchAnalysisRepository.findByPuuidOrderByCreatedAtDesc(puuid);
        if (!recentRecords.isEmpty()) {
            MatchAnalysis recentRecord = recentRecords.get(0);
            Map<String, Object> requestData = recentRecord.getAiRequestData();
            if (requestData != null) {
                if (requestData.containsKey("gameName")) {
                    gameName = (String) requestData.get("gameName");
                }
                if (requestData.containsKey("tagLine")) {
                    tagLine = (String) requestData.get("tagLine");
                }
            }
            if (recentRecord.getTargetPlayerName() != null && !recentRecord.getTargetPlayerName().trim().isEmpty()) {
                gameName = recentRecord.getTargetPlayerName();
            }
        }
        
        logger.info("다중 분석 대상: {}#{}, {} 게임", gameName, tagLine, matchCount);
        
        // 새 분석 레코드 생성
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setPuuid(puuid);
        analysis.setTargetPlayerName(gameName);
        analysis.setAnalysisStatus(AnalysisStatus.REQUESTED);
        // matchId는 null로 설정 (다중 매치이므로)
        
        // 요청 데이터 구성
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("step", "MULTIPLE_MATCH_ANALYSIS");
        requestData.put("puuid", puuid);
        requestData.put("gameName", gameName);
        requestData.put("tagLine", tagLine);
        requestData.put("matchCount", matchCount);
        requestData.put("timestamp", System.currentTimeMillis());
        analysis.setAiRequestData(requestData);
        
        try {
            analysis = matchAnalysisRepository.save(analysis);
            logger.info("다중 분석 레코드 생성 완료: ID={}", analysis.getId());
            
            // 상태를 PROCESSING으로 변경
            analysis.setAnalysisStatus(AnalysisStatus.PROCESSING);
            matchAnalysisRepository.save(analysis);
            
            // GameAnalysisService를 통한 다중 매치 분석
            logger.info("GameAnalysisService를 통한 다중 매치 분석 시작...");
            String aiAnalysisResult = gameAnalysisService.analyzePlayerMultipleMatches(gameName, tagLine, matchCount);
            
            logger.info("다중 매치 AI 분석 완료! 결과 길이: {} 문자", aiAnalysisResult.length());
            
            // AI 응답 데이터 구성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("analysisResult", aiAnalysisResult);
            responseData.put("analysisTimestamp", System.currentTimeMillis());
            responseData.put("analysisMethod", "GameAnalysisService_Multiple");
            responseData.put("matchCount", matchCount);
            responseData.put("puuid", puuid);
            responseData.put("analysisType", "MULTIPLE_MATCH");
            
            // 결과 저장
            analysis.setAiResponseData(responseData);
            analysis.setAnalysisSummary(aiAnalysisResult);
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
            
            // 요청 데이터에 분석 완료 표시 추가
            requestData.put("multipleAnalysisCompleted", true);
            requestData.put("analysisCompletedTimestamp", System.currentTimeMillis());
            analysis.setAiRequestData(requestData);
            
            analysis = matchAnalysisRepository.save(analysis);
            logger.info("다중 매치 분석 결과 저장 완료! 레코드 ID: {}", analysis.getId());
            
        } catch (Exception e) {
            logger.error("다중 매치 AI 분석 실패: ", e);
            analysis.setAnalysisStatus(AnalysisStatus.FAILED);
            analysis.setErrorMessage("다중 매치 AI 분석 실패: " + e.getMessage());
            matchAnalysisRepository.save(analysis);
            throw new RuntimeException("다중 매치 AI 분석 실패", e);
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
            if (AnalysisStatus.COMPLETED.equals(existing.getAnalysisStatus())) {
                logger.info("이미 완료된 분석이 존재함: ID={}", existing.getId());
                return existing;
            }
        }
        
        // 해당 레코드 조회 또는 생성
        MatchAnalysis analysis = findOrCreateAnalysisRecord(puuid, matchId);
        
        try {
            // 상태를 PROCESSING으로 변경
            analysis.setAnalysisStatus(AnalysisStatus.PROCESSING);
            matchAnalysisRepository.save(analysis);
            
            // AI 분석 수행
            String aiAnalysisResult = performGameAnalysis(analysis, puuid, matchId);
            
            // 결과 저장
            saveAnalysisResult(analysis, aiAnalysisResult, puuid, matchId);
            
            logger.info("AI 분석 결과 저장 완료! 레코드 ID: {}", analysis.getId());
            
        } catch (Exception e) {
            logger.error("AI 분석 실패: ", e);
            analysis.setAnalysisStatus(AnalysisStatus.FAILED);
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
        analysis.setAnalysisStatus(AnalysisStatus.REQUESTED);
        
        Map<String, Object> data = new HashMap<>();
        data.put("step", "AI_ANALYSIS_DIRECT");
        data.put("puuid", puuid);
        data.put("matchId", matchId);
        data.put("timestamp", System.currentTimeMillis());
        analysis.setAiRequestData(data);
        
        return matchAnalysisRepository.save(analysis);
    }
    /*DB 저장 로직*/
    private void saveMatchDataToDB(String matchId, Map<String, Object> matchData, Map<String, Object> timelineData) {
        try {
            logger.info("=== 매치 데이터 DB 저장 시작: {} ===", matchId);
            logger.info("matchData 크기: {}", matchData != null ? matchData.size() : 0);
            logger.info("timelineData 크기: {}", timelineData != null ? timelineData.size() : 0);
            
            // 1. Match 엔티티 저장
            logger.info("1단계: Match 엔티티 생성 시작");
            Match match = new Match();
            match.setMatchId(matchId);
            
            // null 체크 추가
            Object gameDurationObj = matchData.get("gameDuration");
            if (gameDurationObj != null) {
                match.setGameDuration((Long) gameDurationObj);
                logger.info("gameDuration 설정: {}", gameDurationObj);
            } else {
                logger.warn("gameDuration이 null입니다");
                match.setGameDuration(0L);
            }
            
            Object gameModeObj = matchData.get("gameMode");
            if (gameModeObj != null) {
                match.setGameMode((String) gameModeObj);
                logger.info("gameMode 설정: {}", gameModeObj);
            } else {
                logger.warn("gameMode가 null입니다");
                match.setGameMode("Unknown");
            }
            
            Object gameVersionObj = matchData.get("gameVersion");
            if (gameVersionObj != null) {
                match.setGameVersion((String) gameVersionObj);
                logger.info("gameVersion 설정: {}", gameVersionObj);
            } else {
                logger.warn("gameVersion이 null입니다");
                match.setGameVersion("Unknown");
            }
            
            Object queueIdObj = matchData.get("queueId");
            if (queueIdObj != null) {
                match.setQueueId((Integer) queueIdObj);
                logger.info("queueId 설정: {}", queueIdObj);
            } else {
                logger.warn("queueId가 null입니다");
                match.setQueueId(0);
            }

            // Match 엔티티 저장
            match = matchRepository.save(match);
            logger.info("Match 엔티티 저장 완료: matchId={}", match.getMatchId());

            // Participant 엔티티들 저장
            logger.info("2단계: Participant 엔티티들 생성 시작");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> participantsData = (List<Map<String, Object>>) matchData.get("participants");

            if (participantsData != null) {
                logger.info("참가자 수: {}", participantsData.size());
                
                for (int i = 0; i < participantsData.size(); i++) {
                    Map<String, Object> participantData = participantsData.get(i);
                    logger.info("참가자 {} 처리 중", i + 1);
                    
                    try {
                        Participant participant = new Participant();
                        participant.setMatch(match);
                        
                        // 각 필드별 null 체크 및 안전한 변환
                        Object participantIdObj = participantData.get("participantId");
                        if (participantIdObj != null) {
                            participant.setParticipantId((Integer) participantIdObj);
                        } else {
                            logger.warn("participantId가 null입니다. 기본값 0 설정");
                            participant.setParticipantId(0);
                        }
                        
                        Object puuidObj = participantData.get("puuid");
                        if (puuidObj != null) {
                            participant.setPuuid((String) puuidObj);
                        } else {
                            logger.warn("puuid가 null입니다. 기본값 설정");
                            participant.setPuuid("unknown");
                        }
                        
                        Object gameNameObj = participantData.get("riotIdGameName");
                        if (gameNameObj != null) {
                            participant.setRiotIdGameName((String) gameNameObj);
                        } else {
                            participant.setRiotIdGameName("Unknown");
                        }
                        
                        Object taglineObj = participantData.get("riotIdTagline");
                        if (taglineObj != null) {
                            participant.setRiotIdTagline((String) taglineObj);
                        } else {
                            participant.setRiotIdTagline("Unknown");
                        }
                        
                        Object summonerNameObj = participantData.get("summonerName");
                        if (summonerNameObj != null) {
                            participant.setSummonerName((String) summonerNameObj);
                        } else {
                            participant.setSummonerName("Unknown");
                        }
                        
                        Object championNameObj = participantData.get("championName");
                        if (championNameObj != null) {
                            participant.setChampionName((String) championNameObj);
                        } else {
                            participant.setChampionName("Unknown");
                        }
                        
                        // 숫자 필드들
                        participant.setKills(getIntegerSafely(participantData, "kills", 0));
                        participant.setDeaths(getIntegerSafely(participantData, "deaths", 0));
                        participant.setAssists(getIntegerSafely(participantData, "assists", 0));
                        participant.setTotalDamageDealtToChampions(getIntegerSafely(participantData, "totalDamageDealtToChampions", 0));
                        participant.setTotalDamageTaken(getIntegerSafely(participantData, "totalDamageTaken", 0));
                        participant.setVisionScore(getIntegerSafely(participantData, "visionScore", 0));
                        participant.setGoldEarned(getIntegerSafely(participantData, "goldEarned", 0));
                        participant.setTotalMinionsKilled(getIntegerSafely(participantData, "totalMinionsKilled", 0));
                        participant.setNeutralMinionsKilled(getIntegerSafely(participantData, "neutralMinionsKilled", 0));
                        participant.setTeamId(getIntegerSafely(participantData, "teamId", 0));
                        
                        Object winObj = participantData.get("win");
                        if (winObj != null) {
                            participant.setWin((Boolean) winObj);
                        } else {
                            participant.setWin(false);
                        }

                        // RiotUser 관계 설정 (수정된 부분)
                        String puuid = participant.getPuuid();
                        if (puuid != null && !puuid.trim().isEmpty() && !"unknown".equals(puuid)) {
                            try {
                                // Optional.ofNullable() 사용
                                RiotUser existingRiotUser = riotUserRepository.findByPuuid(puuid);
                                if (existingRiotUser != null) {
                                    participant.setRiotUser(existingRiotUser);
                                    logger.info("기존 RiotUser 연결: {}", puuid);
                                } else {
                                    // RiotUser가 없으면 새로 생성
                                    RiotUser riotUser = new RiotUser();
                                    riotUser.setPuuid(puuid);
                                    riotUser.setGameName(participant.getRiotIdGameName());
                                    riotUser.setTagLine(participant.getRiotIdTagline());
                                    riotUser = riotUserRepository.save(riotUser);
                                    participant.setRiotUser(riotUser);
                                    logger.info("새 RiotUser 생성 및 연결: {}", puuid);
                                }
                            } catch (Exception e) {
                                logger.warn("RiotUser 관계 설정 실패: {}", e.getMessage());
                                // RiotUser 관계 없이 계속 진행
                            }
                        }
                        
                        participant = participantRepository.save(participant);
                        logger.info("Participant 저장 완료: ID={}, Champion={}", participant.getParticipantId(), participant.getChampionName());
                        
                    } catch (Exception e) {
                        logger.error("참가자 {} 저장 실패: {}", i + 1, e.getMessage(), e);
                        // 개별 참가자 실패는 전체 프로세스를 중단하지 않음
                    }
                }
            } else {
                logger.warn("participants 데이터가 null입니다");
            }

            // MatchTimeline 엔티티들 저장
            logger.info("3단계: MatchTimeline 엔티티들 생성 시작");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> frames = (List<Map<String, Object>>) timelineData.get("frames");

            if (frames != null) {
                logger.info("타임라인 프레임 수: {}", frames.size());
                
                for (int i = 0; i < frames.size(); i++) {
                    Map<String, Object> frameData = frames.get(i);
                    logger.info("프레임 {} 처리 중", i + 1);
                    
                    try {
                        MatchTimeline timeline = new MatchTimeline();
                        timeline.setMatch(match);
                        
                        // Set composite key fields
                        timeline.setMatchId(match.getMatchId());
                        
                        Object timestampObj = frameData.get("timestamp");
                        long timestamp;
                        if (timestampObj != null) {
                            timestamp = (Long) timestampObj;
                            timeline.setTimestamp(timestamp);
                        } else {
                            timestamp = 0L;
                            timeline.setTimestamp(timestamp);
                        }

                        timeline = matchTimelineRepository.save(timeline);
                        logger.info("MatchTimeline 저장 완료: timestamp={}", timeline.getTimestamp());

                        // ParticipantFrame 엔티티들 저장
                        @SuppressWarnings("unchecked")
                        Map<String, Object> participantFramesData = (Map<String, Object>) frameData.get("participantFrames");

                        if (participantFramesData != null) {
                            for (Map.Entry<String, Object> entry : participantFramesData.entrySet()) {
                                try {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> frameInfo = (Map<String, Object>) entry.getValue();

                                    ParticipantFrame participantFrame = new ParticipantFrame();
                                    participantFrame.setTimeline(timeline);
                                    
                                    // Set composite key fields
                                    participantFrame.setMatchId(match.getMatchId());
                                    participantFrame.setTimestamp(timestamp);
                                    participantFrame.setParticipantId((byte) Integer.parseInt(entry.getKey()));

                                    // position 정보
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> position = (Map<String, Object>) frameInfo.get("position");
                                    if (position != null) {
                                        participantFrame.setX(getIntegerSafely(position, "x", 0));
                                        participantFrame.setY(getIntegerSafely(position, "y", 0));
                                    } else {
                                        participantFrame.setX(0);
                                        participantFrame.setY(0);
                                    }
                                    
                                    // 기타 정보들
                                    participantFrame.setTotalGold(getIntegerSafely(frameInfo, "totalGold", 0));
                                    participantFrame.setLevel(getIntegerSafely(frameInfo, "level", 1));
                                    participantFrame.setMinionsKilled(getIntegerSafely(frameInfo, "minionsKilled", 0));
                                    participantFrame.setJungleMinionsKilled(getIntegerSafely(frameInfo, "jungleMinionsKilled", 0));

                                    participantFrameRepository.save(participantFrame);
                                } catch (Exception e) {
                                    logger.warn("ParticipantFrame 저장 실패: {}", e.getMessage());
                                }
                            }
                        }
                        
                        // MatchEvent 엔티티들 저장
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> eventsData = (List<Map<String, Object>>) frameData.get("events");

                        if (eventsData != null) {
                            for (int eventIndex = 0; eventIndex < eventsData.size(); eventIndex++) {
                                Map<String, Object> eventData = eventsData.get(eventIndex);
                                try {
                                    MatchEvent event = new MatchEvent();
                                    event.setTimeline(timeline);
                                    
                                    // Set composite key fields
                                    event.setMatchId(match.getMatchId());
                                    event.setSequenceId((short) eventIndex);
                                    
                                    // Set event timestamp for composite key (use frame timestamp for consistency)
                                    event.setTimestamp(timestamp);
                                    
                                    Object typeObj = eventData.get("type");
                                    if (typeObj != null) {
                                        String eventTypeName = (String) typeObj;
                                        try {
                                            event.setEventTypeId(EventType.getEventTypeId(eventTypeName));
                                        } catch (IllegalArgumentException e) {
                                            // Unknown event type, use a default or skip
                                            event.setEventTypeId((byte) 0); // 0 for unknown
                                        }
                                    } else {
                                        event.setEventTypeId((byte) 0); // 0 for unknown
                                    }
                                    
                                    // Note: Event timestamp already set above for composite key consistency
                                    
                                    event.setParticipantId(getIntegerSafely(eventData, "participantId", 0));

                                    // 선택적 필드들
                                    if (eventData.containsKey("killerId")) {
                                        event.setKillerId(getIntegerSafely(eventData, "killerId", 0));
                                    }
                                    if (eventData.containsKey("victimId")) {
                                        event.setVictimId(getIntegerSafely(eventData, "victimId", 0));
                                    }
                                    if (eventData.containsKey("assistingParticipantIds")) {
                                        @SuppressWarnings("unchecked")
                                        List<Integer> assistingIds = (List<Integer>) eventData.get("assistingParticipantIds");
                                        event.setAssistingParticipantIds(assistingIds);
                                    }
                                    // Note: monsterType, buildingType, laneType, towerType fields removed
                                    // These were not essential for our analysis context
                                    if (eventData.containsKey("itemId")) {
                                        event.setItemId(getIntegerSafely(eventData, "itemId", 0));
                                    }

                                    matchEventRepository.save(event);
                                } catch (Exception e) {
                                    logger.warn("MatchEvent 저장 실패: {}", e.getMessage());
                                }
                            }
                        }
                        
                    } catch (Exception e) {
                        logger.error("프레임 {} 처리 실패: {}", i + 1, e.getMessage(), e);
                    }
                }
            } else {
                logger.warn("frames 데이터가 null입니다");
            }
            
            logger.info("=== 매치 데이터 DB 저장 완료: {} ===", matchId);

        } catch (Exception e) {
            logger.error("매치 데이터 DB 저장 실패: {}", matchId, e);
            throw new RuntimeException("매치 데이터 저장 실패", e);
        }
    }

    /**
     * 안전한 Integer 변환 헬퍼 메서드
     */
    private Integer getIntegerSafely(Map<String, Object> data, String key, Integer defaultValue) {
        try {
            Object value = data.get(key);
            if (value != null) {
                if (value instanceof Integer) {
                    return (Integer) value;
                } else if (value instanceof Long) {
                    return ((Long) value).intValue();
                } else if (value instanceof String) {
                    return Integer.parseInt((String) value);
                } else {
                    return defaultValue;
                }
            }
            return defaultValue;
        } catch (Exception e) {
            logger.warn("{} 값을 Integer로 변환 실패: {}", key, e.getMessage());
            return defaultValue;
        }
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

        //Riot API 데이터를 먼저 DB에 저장
        try{
            //RiotService를 통해 매치 데이터 가져오기
            MatchDetailDto matchDetailDto = riotService.getMatchDetail(matchId);
            MatchTimelineDto matchTimelineDto = riotService.getMatchTimeline(matchId);

            // DTO를 Map으로 변환
            Map<String, Object> matchData = convertMatchDetailToMap(matchDetailDto);
            Map<String, Object> timelineData = convertMatchTimelineToMap(matchTimelineDto);

            // DB에 저장
            saveMatchDataToDB(matchId, matchData, timelineData);

        } catch (Exception e) {
            logger.warn("매치 데이터 DB 저장 실패, AI 분석은 계속 진행: {}", e.getMessage());
        }


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
        analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
        
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
    public List<MatchAnalysis> getAnalysisByStatus(String status) {
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
     * 다중 매치 AI 분석 수행 후 컨트롤러 응답용 Map 반환 (편의 메소드)
     */
    public Map<String, Object> performMultipleAIAnalysisAndGetResponse(String puuid, int matchCount) {
        MatchAnalysis result = performMultipleAIAnalysis(puuid, matchCount);
        return createAnalysisResponseMap(result);
    }

    /**
     * AI 분석 수행 후 컨트롤러 응답용 Map 반환 (편의 메소드)
     */
    public Map<String, Object> performAIAnalysisAndGetResponse(String puuid, String matchId) {
        MatchAnalysis result = performAIAnalysis(puuid, matchId);
        return createAnalysisResponseMap(result);
    }
}
