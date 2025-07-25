package org.mtvs.backend.gemini.service;

import org.mtvs.backend.analysis.analyzer.MapAnalyzer;
import org.mtvs.backend.gemini.prompt.DuoAnalysisPrompt;
import org.mtvs.backend.global.cache.SessionCache;
import org.mtvs.backend.riot.dto.*;
import org.mtvs.backend.riot.service.RiotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class GameAnalysisService {
    
    /**
     * 매치 데이터 번들 클래스 (배치 처리 최적화용)
     */
    private static class MatchDataBundle {
        private final String matchId;
        private final MatchDetailDto detail;
        private final MatchTimelineDto timeline;
        
        public MatchDataBundle(String matchId, MatchDetailDto detail, MatchTimelineDto timeline) {
            this.matchId = matchId;
            this.detail = detail;
            this.timeline = timeline;
        }
        
        public String getMatchId() { return matchId; }
        public MatchDetailDto getDetail() { return detail; }
        public MatchTimelineDto getTimeline() { return timeline; }
    }
    
    private final RiotService riotService;
    private final GeminiService geminiService;
    private final DuoAnalysisPrompt duoAnalysisPrompt;
    private final List<MapAnalyzer> mapAnalyzers;
    private final SessionCache sessionCache;
    
    @Autowired
    @Qualifier("riotApiExecutor")
    private TaskExecutor riotApiExecutor;
    
    public GameAnalysisService(RiotService riotService, GeminiService geminiService, DuoAnalysisPrompt duoAnalysisPrompt, List<MapAnalyzer> mapAnalyzers, SessionCache sessionCache) {
        this.riotService = riotService;
        this.geminiService = geminiService;
        this.duoAnalysisPrompt = duoAnalysisPrompt;
        this.mapAnalyzers = mapAnalyzers;
        this.sessionCache = sessionCache;
    }
    
    /**
     * 플레이어의 최근 매치 목록을 조회하고 요약 정보 제공
     */
    public Map<String, Object> getPlayerMatches(String gameName, String tagLine, int count) {
        try {
            System.out.println("=== 플레이어 매치 목록 조회 시작 ===");
            System.out.println("플레이어: " + gameName + "#" + tagLine);
            
            // 1. 계정 정보 조회
            AccountDto account = riotService.getAccountInfo(gameName, tagLine);
            System.out.println("PUUID: " + account.getPuuid());
            
            // 2. 최근 매치 ID 목록 조회
            List<String> matchIds = riotService.getMatchIds(account.getPuuid(), 0, count);
            System.out.println("조회된 매치 수: " + matchIds.size());
            
            // 3. 각 매치의 요약 정보 생성
            List<Map<String, Object>> matchSummaries = new ArrayList<>();
            
            for (String matchId : matchIds) {
                try {
                    MatchDetailDto matchDetail = riotService.getMatchDetail(matchId);
                    Map<String, Object> summary = createMatchSummary(matchDetail, account.getPuuid(), matchId);
                    matchSummaries.add(summary);
                    System.out.println("매치 요약 생성 완료: " + matchId);
                } catch (Exception e) {
                    System.err.println("매치 " + matchId + " 요약 생성 실패: " + e.getMessage());
                }
            }
            
            // 4. 결과 반환
            Map<String, Object> result = new HashMap<>();
            result.put("playerInfo", Map.of(
                "gameName", gameName,
                "tagLine", tagLine,
                "puuid", account.getPuuid()
            ));
            result.put("matches", matchSummaries);
            result.put("totalMatches", matchSummaries.size());
            
            System.out.println("=== 매치 목록 조회 완료 ===");
            return result;
            
        } catch (Exception e) {
            System.err.println("플레이어 매치 조회 중 오류: " + e.getMessage());
            throw new RuntimeException("매치 목록 조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 플레이어의 최근 N개 매치 종합 분석 (1~5개 제한)
     * @param gameName 게임 닉네임
     * @param tagLine 태그라인 
     * @param matchCount 분석할 매치 수 (1~5개)
     */
    public String analyzePlayerMultipleMatches(String gameName, String tagLine, int matchCount) {
        long startTime = System.currentTimeMillis();
        try {
            System.out.println("=== 다중 매치 종합 분석 시작 ===");
            System.out.println("플레이어: " + gameName + "#" + tagLine);
            System.out.println("분석할 매치 수: " + matchCount);
            System.out.println("시작 시간: " + new Date());
            
            // 입력값 검증
            if (matchCount < 1 || matchCount > 5) {
                throw new IllegalArgumentException("매치 개수는 1~5개만 가능합니다. 입력값: " + matchCount);
            }
            
            // 1. 계정 정보 조회
            long accountStartTime = System.currentTimeMillis();
            AccountDto account = riotService.getAccountInfo(gameName, tagLine);
            System.out.println("계정 정보 조회 완료: " + (System.currentTimeMillis() - accountStartTime) + "ms");
            
            // 2. 최근 매치 목록 조회 (기존 메소드 활용)
            long matchIdsStartTime = System.currentTimeMillis();
            List<String> matchIds = riotService.getMatchIds(account.getPuuid(), 0, matchCount);
            System.out.println("매치 목록 조회 완료: " + (System.currentTimeMillis() - matchIdsStartTime) + "ms");
            
            if (matchIds.isEmpty()) {
                return "분석할 수 있는 매치 데이터가 없습니다.";
            }
            
            System.out.println("실제 분석 대상 매치 수: " + matchIds.size());
            
            // 3. 매치 데이터를 최적화된 병렬 배치로 수집 (세션 캐시 활용)
            long parallelStartTime = System.currentTimeMillis();
            System.out.println("최적화된 병렬 배치 데이터 수집 시작...");
            
            // 3-1. 매치별로 Detail + Timeline을 하나의 작업 단위로 병렬 처리 (효율적 배치 처리)
            List<CompletableFuture<MatchDataBundle>> matchDataFutures = matchIds.stream()
                .map(matchId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("매치 데이터 배치 조회 중: " + matchId);
                        // 세션 캐시를 활용하여 중복 호출 방지
                        MatchDetailDto detail = getMatchDetailWithCache(matchId);
                        MatchTimelineDto timeline = getMatchTimelineWithCache(matchId);
                        return new MatchDataBundle(matchId, detail, timeline);
                    } catch (Exception e) {
                        System.err.println("매치 데이터 조회 실패: " + matchId + " - " + e.getMessage());
                        throw new RuntimeException("매치 데이터 조회 실패: " + matchId, e);
                    }
                }, riotApiExecutor))
                .collect(Collectors.toList());
            
            // 3-2. 모든 비동기 작업 완료 대기 및 결과 수집 (배치 처리 결과)
            List<MatchDetailDto> matchDetails = new ArrayList<>();
            List<MatchTimelineDto> matchTimelines = new ArrayList<>();
            List<String> successfulMatches = new ArrayList<>();
            
            for (CompletableFuture<MatchDataBundle> future : matchDataFutures) {
                try {
                    MatchDataBundle bundle = future.join();
                    
                    // 데이터 검증 (null 체크)
                    if (bundle.getDetail() == null || bundle.getTimeline() == null) {
                        System.err.println("매치 데이터가 null입니다: " + bundle.getMatchId());
                        continue;
                    }
                    
                    matchDetails.add(bundle.getDetail());
                    matchTimelines.add(bundle.getTimeline());
                    successfulMatches.add(bundle.getMatchId());
                    System.out.println("매치 " + successfulMatches.size() + "/" + matchIds.size() + " 수집 완료: " + bundle.getMatchId());
                    
                } catch (Exception e) {
                    System.err.println("매치 처리 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("병렬 데이터 수집 완료. 성공: " + successfulMatches.size() + "/" + matchIds.size());
            System.out.println("병렬 수집 소요 시간: " + (System.currentTimeMillis() - parallelStartTime) + "ms");
            
            // 3-4. 수집된 데이터로 플레이어 데이터 생성
            List<Map<String, Object>> allMatchData = new ArrayList<>();
            
            for (int i = 0; i < matchDetails.size(); i++) {
                try {
                    Map<String, Object> playerData = extractPlayerData(
                        matchDetails.get(i), 
                        matchTimelines.get(i), 
                        account.getPuuid()
                    );
                    playerData.put("matchIndex", i + 1);
                    playerData.put("matchId", successfulMatches.get(i));
                    allMatchData.add(playerData);
                    
                } catch (Exception e) {
                    System.err.println("플레이어 데이터 추출 실패: " + successfulMatches.get(i) + " - " + e.getMessage());
                }
            }
            
            if (allMatchData.isEmpty()) {
                return "모든 매치 데이터 수집에 실패했습니다.";
            }
            
            System.out.println("성공적으로 수집된 매치 수: " + allMatchData.size());
            
            // 4. 종합 분석 프롬프트 생성
            long promptStartTime = System.currentTimeMillis();
            String multipleAnalysisPrompt = createMultipleMatchAnalysisPrompt(allMatchData, gameName, tagLine);
            System.out.println("프롬프트 생성 완료: " + (System.currentTimeMillis() - promptStartTime) + "ms");
            System.out.println("프롬프트 길이: " + multipleAnalysisPrompt.length() + "자");

            // 5. Gemini AI 종합 분석 요청
            long geminiStartTime = System.currentTimeMillis();
            System.out.println("Gemini AI 종합 분석 요청 중...");
            String comprehensiveFeedback = geminiService.sendMessage(multipleAnalysisPrompt);
            System.out.println("Gemini AI 분석 완료: " + (System.currentTimeMillis() - geminiStartTime) + "ms");
            
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("=== 다중 매치 종합 분석 완료 ===");
            System.out.println("전체 소요 시간: " + totalTime + "ms (" + (totalTime/1000.0) + "초)");
            return comprehensiveFeedback;
            
        } catch (Exception e) {
            System.err.println("다중 매치 분석 중 오류: " + e.getMessage());
            return "종합 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 특정 매치에서 특정 플레이어의 상세 분석 수행
     */
    public String analyzePlayerMatch(String gameName, String tagLine, String matchId) {
        try {
            System.out.println("=== 개인 매치 분석 시작 ===");
            System.out.println("플레이어: " + gameName + "#" + tagLine);
            System.out.println("매치 ID: " + matchId);
            
            // 1. 계정 정보 조회
            AccountDto account = riotService.getAccountInfo(gameName, tagLine);
            
            // 2. 매치 상세 정보 및 타임라인 조회 (세션 캐시 활용)
            MatchDetailDto matchDetail = getMatchDetailWithCache(matchId);
            MatchTimelineDto matchTimeline = getMatchTimelineWithCache(matchId);
            
            // 3. 특정 플레이어 데이터 추출
            Map<String, Object> playerData = extractPlayerData(matchDetail, matchTimeline, account.getPuuid());
            
            // 3.1. 포지션 기반 라인 상대 찾기 (간단한 방식)
            System.out.println("=== 포지션 기반 상대 탐지 시작 ===");
            ParticipantDto player = findParticipantByPuuid(matchDetail, account.getPuuid());
            ParticipantDto opponent = null;
            if (player != null) {
                String playerPosition = player.getTeamPosition();
                System.out.println("플레이어 정보: " + player.getChampionName() + " (포지션: " + playerPosition + ", 팀: " + player.getTeamId() + ")");
                
                opponent = findOpponentByPosition(matchDetail, playerPosition, player.getTeamId());
                if (opponent != null) {
                    System.out.println("✅ 같은 포지션 상대 발견: " + opponent.getChampionName() + " (포지션: " + opponent.getTeamPosition() + ")");
                } else {
                    System.out.println("❌ 같은 포지션 상대를 찾을 수 없음");
                }
            } else {
                System.out.println("❌ 플레이어 정보를 찾을 수 없음: " + account.getPuuid());
            }
            System.out.println("=== 포지션 기반 상대 탐지 완료 ===");
            
            // 4. 개인 분석용 프롬프트 생성 (포지션 상대 포함)
            String analysisPrompt = createPersonalAnalysisPrompt(playerData, matchId, opponent);

            // 5. Gemini AI 분석 요청
            System.out.println("Gemini AI 분석 요청 중...");
            String feedback = geminiService.sendMessage(analysisPrompt);
            
            System.out.println("=== 개인 매치 분석 완료 ===");
            return feedback;
            
        } catch (Exception e) {
            System.err.println("개인 매치 분석 중 오류: " + e.getMessage());
            return "분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
    
    /**
     * 매치에서 특정 플레이어의 요약 정보 생성
     */
    private Map<String, Object> createMatchSummary(MatchDetailDto matchDetail, String playerPuuid, String matchId) {
        // 해당 플레이어 찾기
        ParticipantDto player = matchDetail.getInfo().getParticipants().stream()
                .filter(p -> playerPuuid.equals(p.getPuuid()))
                .findFirst()
                .orElse(null);
                
        if (player == null) {
            return Map.of("error", "플레이어를 찾을 수 없습니다");
        }
        
        // 게임 시간 변환 (초 → 분:초)
        long durationSeconds = matchDetail.getInfo().getGameDuration();
        String duration = String.format("%d분 %d초", 
            durationSeconds / 60, durationSeconds % 60);
        
        // 큐 타입 변환
        String queueType = riotService.getQueueName(matchDetail.getInfo().getQueueId());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("matchId", matchId);
        summary.put("basicInfo", Map.of(
            "queueType", queueType,
            "champion", player.getChampionName(),
            "result", player.isWin() ? "승리" : "패배",
            "kda", String.format("%d/%d/%d", player.getKills(), player.getDeaths(), player.getAssists()),
            "duration", duration,
            "gameMode", matchDetail.getInfo().getGameMode()
        ));
        summary.put("detailStats", Map.of(
            "cs", player.getTotalMinionsKilled() + player.getNeutralMinionsKilled(),
            "gold", player.getGoldEarned(),
            "damage", player.getTotalDamageDealtToChampions(),
            "damageTaken", player.getTotalDamageTaken(),
            "visionScore", player.getVisionScore()
        ));
        
        return summary;
    }
    
    /**
     * 특정 플레이어의 매치 데이터 추출
     */
    private Map<String, Object> extractPlayerData(MatchDetailDto matchDetail, MatchTimelineDto matchTimeline, String playerPuuid) {
        Map<String, Object> playerData = new HashMap<>();
        
        // 1. 기본 플레이어 정보 추출
        ParticipantDto player = matchDetail.getInfo().getParticipants().stream()
                .filter(p -> playerPuuid.equals(p.getPuuid()))
                .findFirst()
                .orElse(null);
                
        // Debug: API 응답의 실제 JSON 구조 확인을 위한 reflection 사용
        if (!matchDetail.getInfo().getParticipants().isEmpty()) {
            ParticipantDto firstPlayer = matchDetail.getInfo().getParticipants().get(0);
            System.out.println("🔍 DEBUG: API Response Fields Check");
            System.out.println("  Total damage (working): " + firstPlayer.getTotalDamageDealtToChampions());
            System.out.println("  Magic damage (not working): " + firstPlayer.getMagicDamageDealtToChampions());
            System.out.println("  Vision score (working): " + firstPlayer.getVisionScore());
            System.out.println("  Wards placed (not working): " + firstPlayer.getWardsPlaced());
            
            // toString으로 객체 내용 확인
            System.out.println("🔍 Full ParticipantDto toString:");
            System.out.println(firstPlayer.toString());
        }
                
        if (player == null) {
            throw new RuntimeException("플레이어 데이터를 찾을 수 없습니다");
        }
        
        // 2. 기본 정보
        playerData.put("playerInfo", Map.of(
            "summonerName", player.getRiotIdGameName(),
            "championName", player.getChampionName(),
            "participantId", player.getParticipantId(),
            "teamId", player.getTeamId(),
            "teamPosition", player.getTeamPosition(),
            "result", player.isWin() ? "승리" : "패배"
        ));
        
        System.out.println("=== 플레이어 정보 디버깅 ===");
        System.out.println("DTO participandId: " + player.getParticipantId());
        System.out.println("사용할 participantId: " + player.getParticipantId());
        System.out.println("플레이어명: " + player.getRiotIdGameName());
        System.out.println("챔피언: " + player.getChampionName());
        
        // participantId 수정: DTO에서 0이 나오면 올바른 ID 찾기
        int correctParticipantId = findCorrectParticipantId(matchDetail, playerPuuid);
        System.out.println("DTO participantId: " + player.getParticipantId());
        System.out.println("수정된 participantId: " + correctParticipantId);
        
        // 3. 기본 스탯 (고급 데이터 포함)
        Map<String, Object> finalStats = new HashMap<>();
        finalStats.put("kills", player.getKills());
        finalStats.put("deaths", player.getDeaths());
        finalStats.put("assists", player.getAssists());
        finalStats.put("totalCS", player.getTotalMinionsKilled() + player.getNeutralMinionsKilled());
        finalStats.put("goldEarned", player.getGoldEarned());
        finalStats.put("damageDealt", player.getTotalDamageDealtToChampions());
        finalStats.put("damageTaken", player.getTotalDamageTaken());
        finalStats.put("visionScore", player.getVisionScore());
        
        // 세부 피해량 데이터
        finalStats.put("magicDamage", player.getMagicDamageDealtToChampions());
        finalStats.put("physicalDamage", player.getPhysicalDamageDealtToChampions());
        finalStats.put("trueDamage", player.getTrueDamageDealtToChampions());
        
        // 와드 관련 데이터
        finalStats.put("wardsPlaced", player.getWardsPlaced());
        finalStats.put("wardsKilled", player.getWardsKilled());
        finalStats.put("controlWardsPlaced", player.getControlWardsPlaced());
        
        playerData.put("finalStats", finalStats);
        
        // 3.1. 고급 분석 데이터 추가
        playerData.put("advancedCombat", createAdvancedCombatAnalysis(player));
        playerData.put("advancedVision", createAdvancedVisionAnalysis(player));
        playerData.put("communicationData", createCommunicationAnalysis(player));
        
        // 3.2. 핑 데이터 (소통 분석용)
        Map<String, Object> pingData = new HashMap<>();
        pingData.put("allInPings", player.getAllInPings());
        pingData.put("assistMePings", player.getAssistMePings());
        pingData.put("baitPings", player.getBaitPings());
        pingData.put("commandPings", player.getCommandPings());
        pingData.put("dangerPings", player.getDangerPings());
        pingData.put("enemyMissingPings", player.getEnemyMissingPings());
        pingData.put("enemyVisionPings", player.getEnemyVisionPings());
        pingData.put("getBackPings", player.getGetBackPings());
        pingData.put("holdPings", player.getHoldPings());
        pingData.put("needVisionPings", player.getNeedVisionPings());
        pingData.put("onMyWayPings", player.getOnMyWayPings());
        pingData.put("pushPings", player.getPushPings());
        pingData.put("visionClearedPings", player.getVisionClearedPings());
        
        // 총 핑 수 계산
        int totalPings = player.getAllInPings() + player.getAssistMePings() + player.getBaitPings() + 
                        player.getCommandPings() + player.getDangerPings() + player.getEnemyMissingPings() +
                        player.getEnemyVisionPings() + player.getGetBackPings() + player.getHoldPings() +
                        player.getNeedVisionPings() + player.getOnMyWayPings() + player.getPushPings() +
                        player.getVisionClearedPings();
        pingData.put("totalPings", totalPings);
        
        playerData.put("pingData", pingData);
        
        // 4. 게임 정보
        playerData.put("gameInfo", Map.of(
            "duration", matchDetail.getInfo().getGameDuration(),
            "gameMode", matchDetail.getInfo().getGameMode(),
            "queueType", riotService.getQueueName(matchDetail.getInfo().getQueueId())
        ));
        
        // 5. 기본 게임 정보만 추가 (위치 분석 제거)
        playerData.put("mapName", "소환사의 협곡");
        
        // 5.1. 맵별 위치 분석 추가
        int mapId = matchDetail.getInfo().getMapId();
        MapAnalyzer mapAnalyzer = getMapAnalyzer(mapId);
        Map<String, Object> positionAnalysis = mapAnalyzer.analyzePlayerPosition(
            matchTimeline, 
            correctParticipantId, 
            player.getTeamId()
        );
        playerData.put("positionAnalysis", positionAnalysis);
        
        // 6. 기본 이벤트만 추출 (복잡한 분석 제거)
        List<Map<String, Object>> playerEvents = extractBasicPlayerEvents(matchTimeline, correctParticipantId);
        playerData.put("timelineEvents", playerEvents);
        
        return playerData;
    }
    
    /**
     * mapId에 따라 적절한 분석기를 반환
     */
    private MapAnalyzer getMapAnalyzer(int mapId) {
        return mapAnalyzers.stream()
                .filter(analyzer -> analyzer.getSupportedMapId() == mapId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원되지 않는 맵 ID: " + mapId));
    }
    
    /**
     * 세션 캐시를 활용한 매치 상세 정보 조회 (중복 호출 방지)
     */
    private MatchDetailDto getMatchDetailWithCache(String matchId) {
        String cacheKey = "matchDetail:" + matchId;
        MatchDetailDto cached = sessionCache.get(cacheKey, MatchDetailDto.class);
        
        if (cached != null) {
            System.out.println("매치 상세 정보 캐시 히트: " + matchId);
            return cached;
        }
        
        System.out.println("매치 상세 정보 API 호출: " + matchId);
        MatchDetailDto result = riotService.getMatchDetail(matchId);
        sessionCache.put(cacheKey, result);
        return result;
    }
    
    /**
     * 세션 캐시를 활용한 매치 타임라인 조회 (중복 호출 방지)
     */
    private MatchTimelineDto getMatchTimelineWithCache(String matchId) {
        String cacheKey = "matchTimeline:" + matchId;
        MatchTimelineDto cached = sessionCache.get(cacheKey, MatchTimelineDto.class);
        
        if (cached != null) {
            System.out.println("매치 타임라인 캐시 히트: " + matchId);
            return cached;
        }
        
        System.out.println("매치 타임라인 API 호출: " + matchId);
        MatchTimelineDto result = riotService.getMatchTimeline(matchId);
        sessionCache.put(cacheKey, result);
        return result;
    }
    
    // 중요한 이벤트 타입들 - 메모리 사용량 80-90% 절약
    private static final Set<String> IMPORTANT_EVENT_TYPES = Set.of(
        "CHAMPION_KILL",        // 킬/데스/어시스트 (가장 중요)
        "ELITE_MONSTER_KILL",   // 드래곤, 바론, 전령
        "BUILDING_KILL"         // 타워, 억제기 파괴
    );
    
    /**
     * 특정 플레이어 관련 타임라인 이벤트 추출 (맵별 중요 이벤트만)
     */
    private List<Map<String, Object>> extractPlayerEvents(MatchTimelineDto matchTimeline, int participantId, Set<String> importantEventTypes) {
        List<Map<String, Object>> playerEvents = new ArrayList<>();
        
        if (matchTimeline.getInfo() != null && matchTimeline.getInfo().getFrames() != null) {
            for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
                if (frame.getEvents() != null) {
                    for (EventDto event : frame.getEvents()) {
                        // 맵별 중요 이벤트만 필터링 (메모리 80-90% 절약)
                        if (importantEventTypes.contains(event.getType()) && 
                            isPlayerRelatedEvent(event, participantId)) {
                            Map<String, Object> eventData = new HashMap<>();
                            eventData.put("type", event.getType());
                            eventData.put("timestamp", event.getTimestamp());
                            eventData.put("timeMinutes", String.format("%.1f분", event.getTimestamp() / 60000.0));
                            eventData.put("description", createEventDescription(event, participantId));
                            
                            // 이벤트 타입별로 필요한 필드만 추가 (메모리 절약)
                            switch (event.getType()) {
                                case "CHAMPION_KILL":
                                    eventData.put("killerId", event.getKillerId());
                                    eventData.put("victimId", event.getVictimId());
                                    eventData.put("assistingParticipantIds", event.getAssistingParticipantIds());
                                    break;
                                case "ELITE_MONSTER_KILL":
                                    eventData.put("monsterType", event.getMonsterType());
                                    break;
                                case "BUILDING_KILL":
                                    eventData.put("buildingType", event.getBuildingType());
                                    break;
                            }
                            
                            playerEvents.add(eventData);
                        }
                    }
                }
            }
        }
        
        // 시간순 정렬
        playerEvents.sort((e1, e2) -> 
            Long.compare((Long)e1.get("timestamp"), (Long)e2.get("timestamp"))
        );
        
        return playerEvents;
    }
    
    /**
     * 이벤트가 특정 플레이어와 관련이 있는지 확인
     */
    private boolean isPlayerRelatedEvent(EventDto event, int participantId) {
        // 직접 관련된 경우
        if (Objects.equals(event.getParticipantId(), participantId) ||
            Objects.equals(event.getKillerId(), participantId) ||
            Objects.equals(event.getVictimId(), participantId)) {
            return true;
        }
        
        // 어시스트한 경우
        if (event.getAssistingParticipantIds() != null) {
            return event.getAssistingParticipantIds().contains(participantId);
        }
        
        return false;
    }
    
    /**
     * 이벤트 설명 생성
     */
    private String createEventDescription(EventDto event, int participantId) {
        String timeStr = String.format("%.1f분", event.getTimestamp() / 60000.0);
        
        switch (event.getType()) {
            case "CHAMPION_KILL":
                if (Objects.equals(event.getKillerId(), participantId)) {
                    return timeStr + ": 챔피언 킬 달성";
                } else if (Objects.equals(event.getVictimId(), participantId)) {
                    return timeStr + ": 사망";
                } else if (event.getAssistingParticipantIds() != null && 
                          event.getAssistingParticipantIds().contains(participantId)) {
                    return timeStr + ": 킬 어시스트";
                }
                break;
                
            case "ELITE_MONSTER_KILL":
                String monsterType = event.getMonsterType() != null ? event.getMonsterType() : "몬스터";
                return timeStr + ": " + monsterType + " 처치 참여";
                
            case "BUILDING_KILL":
                String buildingType = event.getBuildingType() != null ? event.getBuildingType() : "구조물";
                return timeStr + ": " + buildingType + " 파괴 참여";
                
            case "ITEM_PURCHASED":
                String itemInfo = event.getItemId() != null ? "ID: " + event.getItemId() : "알 수 없는 아이템";
                return timeStr + ": 아이템 구매 (" + itemInfo + ")";
                
            case "LEVEL_UP":
                return timeStr + ": 레벨업";
                
            default:
                String eventType = event.getType() != null ? event.getType() : "알 수 없는 이벤트";
                return timeStr + ": " + eventType;
        }
        
        String fallbackEventType = event.getType() != null ? event.getType() : "알 수 없는 이벤트";
        return timeStr + ": " + fallbackEventType;
    }

    /**
     * 다중 매치 종합 분석용 프롬프트 생성
     */
    private String createMultipleMatchAnalysisPrompt(List<Map<String, Object>> allMatchData, String gameName, String tagLine) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 리그 오브 레전드 데이터를 분석하여 플레이어의 잠재력을 최대로 끌어올리는 세계 최고의 AI 코치입니다. ");
        prompt.append("당신의 목표는 단순히 KDA나 승률 같은 표면적인 데이터를 나열하는 것이 아니라, 플레이어의 위치 데이터와 게임 내 이벤트를 유기적으로 결합하여, ");
        prompt.append("'왜' 그런 플레이를 했는지 전략적, 심리적 관점에서 심층 분석하고, 즉시 실행 가능한 구체적인 행동 개선안을 제시하는 것입니다.\n\n");

        prompt.append("=== 2. 분석 대상 데이터 형식 ===\n");
        prompt.append("당신은 아래와 같은 형식의 데이터를 제공받게 됩니다.\n");
        prompt.append("• 플레이어 정보: 소환사명\n");
        prompt.append("• 게임별 요약: 챔피언, KDA, CS, 딜량, 승패 여부\n");
        prompt.append("• 전체 통계 요약: 평균 KDA, 승률, 평균 CS 등\n");
        prompt.append("• 위치 및 이벤트 데이터 (가장 중요): 각 게임의 시간 흐름에 따른 플레이어의 (X, Y) 좌표, 레벨, 골드, 아이템 구매/파괴, 스킬 레벨업, 그리고 사망(Death) 이벤트 정보.\n\n");

        prompt.append("=== 3. 심층 분석 프레임워크 (5단계) ===\n");
        prompt.append("당신은 반드시 아래 5단계의 사고 과정을 거쳐 피드백을 생성해야 합니다.\n\n");

        prompt.append("**1단계: '결정적 순간' 식별 (Identify Critical Moments)**\n");
        prompt.append("제공된 모든 게임의 위치 및 이벤트 데이터에서 플레이어의 모든 '사망(Death)' 순간을 식별하십시오.\n");
        prompt.append("각 사망 직전 30초 동안의 플레이어 위치, 주변의 아군 및 적군 위치, 미니맵 상의 주요 정보(오브젝트 타이머, 적 정글러의 마지막 위치 등)를 핵심 분석 대상으로 삼습니다.\n\n");

        prompt.append("**2단계: '전략적 Why' 재구성 (Reconstruct the 'Strategic Why')**\n");
        prompt.append("각 '사망' 순간에 대해 아래 질문에 답하여, 전략적으로 왜 죽을 수밖에 없었는지를 명확히 설명하십시오.\n");
        prompt.append("• 위치 선정의 문제: \"사망 당시 플레이어의 위치는 전략적으로 안전한 곳이었습니까? (예: 아군 포탑 근처) 혹은 위험을 자초하는 곳이었습니까? (예: 시야 없는 적 정글, 강 중앙)\"\n");
        prompt.append("• 상황 판단의 문제: \"죽기 직전, 플레이어는 무엇을 하려고 했습니까? (예: 라인 압박, 다이브, 로밍, 오브젝트 획득) 그 판단은 당시 게임 상황(아군 위치, 성장 격차, 오브젝트 유무)에 비추어 볼 때 합리적이었습니까?\"\n");
        prompt.append("• 전략적 실수 요약: \"따라서, 이 죽음의 핵심적인 전략적 실수는 무엇입니까? (예: '무리한 라인 압박으로 인한 갱킹 허용', '아군 백업이 불가능한 상황에서의 고립', '중요 오브젝트를 앞두고 불필요한 교전 시도')\"\n\n");

        prompt.append("**3단계: '심리적 Why' 추론 (Infer the 'Psychological Why')**\n");
        prompt.append("2단계에서 분석한 '전략적 실수'들이 여러 게임에 걸쳐 어떤 패턴으로 반복되는지 관찰하십시오.\n");
        prompt.append("이 반복되는 패턴을 기반으로, 플레이어가 어떤 심리적 상태에 빠지기 쉬운지 추론하십시오.\n");
        prompt.append("• 추론 예시 1: 만약 '솔로 킬 직후, 시야 없이 더 깊게 들어가다 죽는 패턴'이 반복된다면 -> 심리적 추론: \"성공에 도취되어 자신의 강함을 과신하는 '영웅 심리'에 빠져, 리스크 계산을 소홀히 하는 경향이 있습니다.\"\n");
        prompt.append("• 추론 예시 2: 만약 '팀이 불리할 때, 혼자 무리하게 싸움을 걸다 죽는 패턴'이 반복된다면 -> 심리적 추론: \"불리한 상황을 타개해야 한다는 '조급함'과 '압박감' 때문에, 낮은 성공 확률의 싸움에 모든 것을 거는 경향이 있습니다.\"\n");
        prompt.append("• 추론 예시 3: 만약 '같은 갱킹 루트에 반복적으로 당하는 패턴'이 보인다면 -> 심리적 추론: \"한번 당한 실수에서 배우고 플레이를 수정하는 '적응력'이 부족하거나, 감정적으로 '틸트' 상태에 빠져 이성적인 판단을 하지 못하는 경향이 있습니다.\"\n\n");

        prompt.append("=== 플레이어 정보 ===\n");
        prompt.append("소환사명: ").append(gameName).append("#").append(tagLine).append("\n");
        prompt.append("분석 대상: 최근 ").append(allMatchData.size()).append("게임의 종합 성과\n\n");

        // 각 게임별 요약 정보
        prompt.append("=== 게임별 요약 ===\n");

        int totalKills = 0, totalDeaths = 0, totalAssists = 0;
        int totalCS = 0, totalGold = 0, totalDamage = 0;
        int wins = 0;
        List<String> champions = new ArrayList<>();

        for (Map<String, Object> matchData : allMatchData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> playerInfo = (Map<String, Object>) matchData.get("playerInfo");
            @SuppressWarnings("unchecked")
            Map<String, Object> finalStats = (Map<String, Object>) matchData.get("finalStats");
            @SuppressWarnings("unchecked")
            Map<String, Object> gameInfo = (Map<String, Object>) matchData.get("gameInfo");

            int matchIndex = (Integer) matchData.get("matchIndex");
            String champion = (String) playerInfo.get("championName");
            String result = (String) playerInfo.get("result");

            champions.add(champion);
            if ("승리".equals(result)) wins++;

            int kills = (Integer) finalStats.get("kills");
            int deaths = (Integer) finalStats.get("deaths");
            int assists = (Integer) finalStats.get("assists");
            int cs = (Integer) finalStats.get("totalCS");
            int gold = (Integer) finalStats.get("goldEarned");
            int damage = (Integer) finalStats.get("damageDealt");

            totalKills += kills;
            totalDeaths += deaths;
            totalAssists += assists;
            totalCS += cs;
            totalGold += gold;
            totalDamage += damage;

            prompt.append("게임 ").append(matchIndex).append(": ")
                  .append(champion).append(" - ")
                  .append(result).append(" (")
                  .append(kills).append("/").append(deaths).append("/").append(assists).append(", ")
                  .append("CS: ").append(cs).append(", ")
                  .append("딜량: ").append(String.format("%,d", damage)).append(")\n");
        }

        // 전체 통계 요약
        prompt.append("\n=== 전체 통계 요약 ===\n");
        prompt.append("전체 승률: ").append(wins).append("승 ").append(allMatchData.size() - wins).append("패 (")
              .append(String.format("%.1f", (wins * 100.0) / allMatchData.size())).append("%)\n");
        prompt.append("평균 KDA: ")
              .append(String.format("%.1f", (double) totalKills / allMatchData.size())).append("/")
              .append(String.format("%.1f", (double) totalDeaths / allMatchData.size())).append("/")
              .append(String.format("%.1f", (double) totalAssists / allMatchData.size())).append("\n");
        prompt.append("평균 CS: ").append(String.format("%.1f", (double) totalCS / allMatchData.size())).append("\n");
        prompt.append("평균 골드: ").append(String.format("%,d", totalGold / allMatchData.size())).append("\n");
        prompt.append("평균 딜량: ").append(String.format("%,d", totalDamage / allMatchData.size())).append("\n");
        prompt.append("사용 챔피언: ").append(String.join(", ", champions)).append("\n\n");

        // 게임별 상세 위치 및 이벤트 데이터 (가장 중요)
        prompt.append("=== 위치 및 이벤트 데이터 (핵심 분석 대상) ===\n");
        for (Map<String, Object> matchData : allMatchData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> playerInfo = (Map<String, Object>) matchData.get("playerInfo");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> events = (List<Map<String, Object>>) matchData.get("timelineEvents");
            @SuppressWarnings("unchecked")
            Map<String, Object> positionAnalysis = (Map<String, Object>) matchData.get("positionAnalysis");

            int matchIndex = (Integer) matchData.get("matchIndex");
            String champion = (String) playerInfo.get("championName");
            String result = (String) playerInfo.get("result");

            prompt.append("게임 ").append(matchIndex).append(" (").append(champion).append(" - ").append(result).append("):\n");

            // 사망 이벤트 특별 표시
            if (events != null && !events.isEmpty()) {
                prompt.append("  주요 이벤트 (사망 포함):\n");
                for (Map<String, Object> event : events) {
                    String eventType = (String) event.get("type");
                    String timeMinutes = (String) event.get("timeMinutes");
                    String description = (String) event.get("description");

                    // 사망 이벤트 강조
                    if ("사망".equals(description) || description.contains("사망")) {
                        prompt.append("  ⚠️  ").append(timeMinutes).append(" - **사망 이벤트** (중요 분석 대상)\n");
                    } else {
                        prompt.append("    ").append(timeMinutes).append(" - ").append(description).append("\n");
                    }
                }
            }

            // 포지셔닝 위험도 분석
            if (positionAnalysis != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> riskAnalysis = (Map<String, Object>) positionAnalysis.get("riskAnalysis");
                @SuppressWarnings("unchecked")
                Map<String, Integer> zoneTimeSpent = (Map<String, Integer>) positionAnalysis.get("zoneTimeSpent");

                if (riskAnalysis != null) {
                    prompt.append("  위험도 분석: 고위험 ").append(riskAnalysis.get("highRiskPercentage")).append("%, ")
                          .append("안전지역 ").append(riskAnalysis.get("safePercentage")).append("%\n");
                }

                if (zoneTimeSpent != null) {
                    prompt.append("  맵 활동: 적정글 ").append(zoneTimeSpent.get("enemyJungle")).append("분, ")
                          .append("리버 ").append(zoneTimeSpent.get("river")).append("분, ")
                          .append("자정글 ").append(zoneTimeSpent.get("ownJungle")).append("분\n");
                }
            }
            prompt.append("\n");
        }

        // 새로운 분석 프레임워크 요청
        prompt.append("=== 분석 요청: 5단계 심층 분석 수행 ===\n");
        prompt.append("위에서 제시한 3단계 분석 프레임워크에 이어서 다음을 수행하십시오:\n\n");

        prompt.append("**4단계: '패턴 기반 행동 개선안' 도출 (Derive Pattern-Based Action Plans)**\n");
        prompt.append("3단계에서 파악한 심리적 패턴을 기반으로, 구체적이고 즉시 실행 가능한 행동 개선안을 제시하십시오.\n");
        prompt.append("• 개선안 예시: '솔로 킬 후 15초간 반드시 미니맵을 확인하고, 적 정글러가 보이지 않으면 즉시 후퇴'\n");
        prompt.append("• 각 개선안에는 '언제', '어떻게', '왜'를 명확히 포함하십시오.\n\n");

        prompt.append("**5단계: '성장 로드맵' 제시 (Present Growth Roadmap)**\n");
        prompt.append("플레이어의 현재 수준을 정확히 평가하고, 단계별 성장 목표를 제시하십시오.\n");
        prompt.append("• 현재 실력 등급 (Bronze ~ Challenger 기준)\n");
        prompt.append("• 다음 10게임에서 집중할 핵심 3가지\n");
        prompt.append("• 1개월 후 달성 가능한 구체적 목표 (KDA, 승률, 특정 스킬 개선 등)\n\n");

        prompt.append("**중요: 반드시 사망 이벤트를 중심으로 한 위치 기반 분석을 우선시하고, **");
        prompt.append("표면적인 통계보다는 '왜 그런 플레이를 했는가'에 대한 심층적 통찰을 제공하십시오.**");

        return prompt.toString();
    }

    /**
     * 참가자를 PUUID로 찾기
     */
    private ParticipantDto findParticipantByPuuid(MatchDetailDto matchDetail, String puuid) {
        return matchDetail.getInfo().getParticipants().stream()
                .filter(p -> puuid.equals(p.getPuuid()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 개인 분석용 프롬프트 생성 (라인 상대 포함)
     */
    private String createPersonalAnalysisPrompt(Map<String, Object> playerData, String matchId, ParticipantDto opponent) {
        @SuppressWarnings("unchecked")
        Map<String, Object> playerInfo = (Map<String, Object>) playerData.get("playerInfo");
        @SuppressWarnings("unchecked")
        Map<String, Object> finalStats = (Map<String, Object>) playerData.get("finalStats");
        @SuppressWarnings("unchecked")
        Map<String, Object> gameInfo = (Map<String, Object>) playerData.get("gameInfo");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) playerData.get("timelineEvents");

        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 리그 오브 레전드 전문 개인 코치입니다.\n\n");

        // 플레이어 기본 정보
        prompt.append("=== 플레이어 정보 ===\n");
        prompt.append("소환사명: ").append(playerInfo.get("summonerName")).append("\n");
        prompt.append("챔피언: ").append(playerInfo.get("championName")).append("\n");
        prompt.append("게임 결과: ").append(playerInfo.get("result")).append("\n");
        prompt.append("게임 모드: ").append(gameInfo.get("queueType")).append("\n");
        prompt.append("게임 시간: ").append(String.format("%.1f분", (Long)gameInfo.get("duration") / 60.0)).append("\n\n");

        // 최종 스탯
        prompt.append("=== 최종 전적 ===\n");
        prompt.append("KDA: ").append(finalStats.get("kills")).append("/")
              .append(finalStats.get("deaths")).append("/")
              .append(finalStats.get("assists")).append("\n");
        prompt.append("총 CS: ").append(finalStats.get("totalCS")).append("\n");
        prompt.append("획득 골드: ").append(String.format("%,d", (Integer)finalStats.get("goldEarned"))).append("\n");
        prompt.append("챔피언 딜량: ").append(String.format("%,d", (Integer)finalStats.get("damageDealt"))).append("\n");
        prompt.append("받은 피해: ").append(String.format("%,d", (Integer)finalStats.get("damageTaken"))).append("\n");
        prompt.append("시야 점수: ").append(finalStats.get("visionScore")).append("\n\n");

        // 게임 흐름 (주요 이벤트) - 시간순
        prompt.append("=== 게임 흐름 및 주요 이벤트 ===\n");
        if (events != null && !events.isEmpty()) {
            for (Map<String, Object> event : events) {
                prompt.append(event.get("time")).append(" - ")
                      .append(event.get("description")).append("\n");
            }
        } else {
            prompt.append("주요 이벤트 정보가 없습니다.\n");
        }
        prompt.append("\n");

        // 포지션 상대 정보 추가 (간단한 방식)
        if (opponent != null) {
            prompt.append("=== 포지션 매치업 분석 ===\n");
            prompt.append("상대: ").append(opponent.getChampionName())
                  .append(" (").append(opponent.getKills()).append("/")
                  .append(opponent.getDeaths()).append("/")
                  .append(opponent.getAssists()).append(")\n");
            prompt.append("포지션: ").append(opponent.getTeamPosition()).append("\n");
            prompt.append("상대 CS: ").append(opponent.getTotalMinionsKilled() + opponent.getNeutralMinionsKilled()).append("\n");
            prompt.append("상대 골드: ").append(String.format("%,d", opponent.getGoldEarned())).append("원\n");
            prompt.append("상대 딜량: ").append(String.format("%,d", opponent.getTotalDamageDealtToChampions())).append("\n");
            prompt.append("\n");
        }
        
        // 고급 분석 섹션 추가
        prompt.append(createAdvancedAnalysisPrompt(playerData));
        
        // 위치 분석 제거됨 - teamPosition으로 대체

        // 분석 요청
        prompt.append("이 데이터를 바탕으로 다음 항목들을 상세히 분석해주세요:\n\n");

        prompt.append("1. **라인전 및 초반 운영** (0-15분)\n");
        prompt.append("   - 파밍 효율성 및 CS 관리\n");
        prompt.append("   - 킬/데스 패턴 분석\n");
        prompt.append("   - 초반 골드 운영\n\n");

        prompt.append("2. **중반 게임 운영** (15-25분)\n");
        prompt.append("   - 오브젝트 기여도\n");
        prompt.append("   - 팀파이트 참여도\n");
        prompt.append("   - 로밍 및 맵 압박\n\n");

        prompt.append("3. **후반 캐리력** (25분+)\n");
        prompt.append("   - 딜량 기여도\n");
        prompt.append("   - 포지셔닝 및 생존력\n");
        prompt.append("   - 게임 결정력\n\n");

        prompt.append("4. **포지셔닝 및 맵 운영**\n");
        prompt.append("   - 맵 구역별 활동 패턴 평가\n");
        prompt.append("   - 로밍 타이밍과 효율성\n");
        prompt.append("   - 위험 지역 관리 능력\n");
        prompt.append("   - 이동성과 맵 압박 기여도\n\n");

        prompt.append("5. **구체적인 개선 가이드**\n");
        prompt.append("5. **구체적인 개선 가이드**\n");
        prompt.append("   - 즉시 적용 가능한 개선점 3가지\n");
        prompt.append("   - 다음 게임에서 집중할 포인트\n");
        prompt.append("   - 이 챔피언으로 더 잘하는 방법\n");
        prompt.append("   - 포지셔닝 및 맵 운영 개선 방안\n\n");

        prompt.append("각 분석은 구체적인 시간대와 상황을 언급하며, 실행 가능한 조언으로 제공해주세요.\n");
        prompt.append("특히 포지셔닝과 맵 운영 데이터를 활용하여 플레이어의 이동 패턴과 위험 관리 능력을 평가해주세요.\n");
        if (opponent != null) {
            prompt.append("포지션 상대와의 비교를 통해 개선점을 구체적으로 제시해주세요.\n");
        }
        prompt.append("전체적인 평가와 함께 가장 개선이 필요한 부분을 우선순위로 제시해주세요.");

        return prompt.toString();
    }
    
    /**
     * 올바른 participantId 찾기 (PUUID 기준)
     */
    private int findCorrectParticipantId(MatchDetailDto matchDetail, String playerPuuid) {
        if (matchDetail.getInfo() != null && matchDetail.getInfo().getParticipants() != null) {
            List<ParticipantDto> participants = matchDetail.getInfo().getParticipants();
            
            for (int i = 0; i < participants.size(); i++) {
                ParticipantDto participant = participants.get(i);
                if (playerPuuid.equals(participant.getPuuid())) {
                    // 리스트 인덱스는 0부터 시작하지만, participantId는 1부터 시작
                    int participantId = i + 1;
                    System.out.println("PUUID " + playerPuuid + "는 참가자 " + participantId + "번입니다.");
                    return participantId;
                }
            }
        }
        
        System.err.println("해당 PUUID를 가진 참가자를 찾을 수 없습니다: " + playerPuuid);
        return 1; // 기본값
    }
    
    /**
     * 플레이어의 위치 정보 기반 분석 (DEPRECATED - 맵별 분석기로 대체됨)
     */
    @Deprecated
    private Map<String, Object> extractPositionAnalysis(MatchTimelineDto matchTimeline, int participantId) {
        Map<String, Object> positionAnalysis = new HashMap<>();
        List<Map<String, Object>> positionHistory = new ArrayList<>();
        Map<String, Integer> zoneTimeSpent = new HashMap<>();
        
        System.out.println("=== 위치 분석 디버깅 시작 ===");
        System.out.println("참가자 ID: " + participantId);
        
        // 맵 구역별 시간 계산을 위한 초기화
        zoneTimeSpent.put("ownJungle", 0);
        zoneTimeSpent.put("enemyJungle", 0);
        zoneTimeSpent.put("topLane", 0);
        zoneTimeSpent.put("midLane", 0);
        zoneTimeSpent.put("botLane", 0);
        zoneTimeSpent.put("river", 0);
        zoneTimeSpent.put("unknown", 0);
        
        if (matchTimeline.getInfo() != null && matchTimeline.getInfo().getFrames() != null) {
            System.out.println("총 프레임 수: " + matchTimeline.getInfo().getFrames().size());
            
            for (int i = 0; i < matchTimeline.getInfo().getFrames().size(); i++) {
                FrameDto frame = matchTimeline.getInfo().getFrames().get(i);
                System.out.println("프레임 " + i + " 처리 중, 타임스탬프: " + frame.getTimestamp());
                
                if (frame.getParticipantFrames() != null) {
                    System.out.println("participantFrames 키들: " + frame.getParticipantFrames().keySet());
                    
                    // 다양한 키 형태로 시도
                    ParticipantFrameDto playerFrame = null;
                    
                    // 방법 1: participantId를 String으로 변환
                    playerFrame = frame.getParticipantFrames().get(String.valueOf(participantId));
                    if (playerFrame == null) {
                        // 방법 2: 0부터 시작하는 인덱스인지 확인
                        playerFrame = frame.getParticipantFrames().get(String.valueOf(participantId - 1));
                    }
                    
                    if (playerFrame != null) {
                        System.out.println("플레이어 프레임 발견!");
                        System.out.println("X: " + playerFrame.getPosition().getX() + ", Y: " + playerFrame.getPosition().getY());
                        System.out.println("레벨: " + playerFrame.getLevel() + ", 골드: " + playerFrame.getTotalGold());
                        
                        int x = playerFrame.getPosition().getX();
                        int y = playerFrame.getPosition().getY();
                        long timestamp = frame.getTimestamp();
                        
                        if (x > 0 && y > 0) {
                            // 위치 정보 기록
                            Map<String, Object> positionData = new HashMap<>();
                            positionData.put("timestamp", timestamp);
                            positionData.put("timeMinutes", String.format("%.1f분", timestamp / 60000.0));
                            positionData.put("x", x);
                            positionData.put("y", y);
                            positionData.put("zone", determineMapZone(x, y, participantId));
                            positionData.put("level", playerFrame.getLevel());
                            positionData.put("gold", playerFrame.getTotalGold());
                            
                            positionHistory.add(positionData);
                            
                            // 구역별 시간 누적 (1분마다 측정되므로 약 1분씩 추가)
                            String zone = determineMapZone(x, y, participantId);
                            zoneTimeSpent.put(zone, zoneTimeSpent.get(zone) + 1);
                            
                            System.out.println("위치 데이터 추가: " + x + ", " + y + " -> " + zone);
                        } else {
                            System.out.println("위치 데이터가 0이거나 음수: X=" + x + ", Y=" + y);
                        }
                    } else {
                        System.out.println("participantId " + participantId + "에 대한 프레임을 찾을 수 없음");
                        // 전체 키를 출력해서 구조 파악
                        if (i < 3) { // 처음 3개 프레임만 상세 출력
                            for (String key : frame.getParticipantFrames().keySet()) {
                                ParticipantFrameDto anyFrame = frame.getParticipantFrames().get(key);
                                System.out.println("키 '" + key + "': X=" + anyFrame.getPosition().getX() + ", Y=" + anyFrame.getPosition().getY() +
                                                 ", 레벨=" + anyFrame.getLevel());
                            }
                        }
                    }
                } else {
                    System.out.println("participantFrames가 null임");
                }
            }
        } else {
            System.out.println("matchTimeline.getInfo() 또는 getFrames()가 null임");
        }
        
        System.out.println("총 위치 기록 수: " + positionHistory.size());
        System.out.println("=== 위치 분석 디버깅 끝 ===");
        
        // 위치 패턴 분석
        Map<String, Object> patterns = analyzeMovementPatterns(positionHistory);
        
        // 위험도 분석 
        Map<String, Object> riskAnalysis = analyzePositionRisk(positionHistory, participantId);
        
        positionAnalysis.put("positionHistory", positionHistory);
        positionAnalysis.put("zoneTimeSpent", zoneTimeSpent);
        positionAnalysis.put("movementPatterns", patterns);
        positionAnalysis.put("riskAnalysis", riskAnalysis);
        positionAnalysis.put("totalPositionRecords", positionHistory.size());
        
        return positionAnalysis;
    }
    
    /**
     * 맵 좌표를 기반으로 구역 판단
     */
    private String determineMapZone(int x, int y, int participantId) {
        // LOL 맵 구역 분석 (대략적인 좌표)
        boolean isBlueTeam = participantId <= 5; // 1-5번이 블루팀, 6-10번이 레드팀
        
        // 리버 구역 (맵 중앙)
        if (x >= 6500 && x <= 8500 && y >= 6500 && y <= 8500) {
            return "river";
        }
        
        // 상단 (탑 라인)
        if (y > 10000) {
            return "topLane";
        }
        
        // 하단 (봇 라인)
        if (y < 5000) {
            return "botLane";
        }
        
        // 중앙 (미드 라인)
        if (x >= 6000 && x <= 9000 && y >= 5000 && y <= 10000) {
            return "midLane";
        }
        
        // 정글 구역
        if (isBlueTeam) {
            // 블루팀 기준
            if (x < 7500) {
                return "ownJungle"; // 자팀 정글
            } else {
                return "enemyJungle"; // 적팀 정글
            }
        } else {
            // 레드팀 기준
            if (x > 7500) {
                return "ownJungle"; // 자팀 정글
            } else {
                return "enemyJungle"; // 적팀 정글
            }
        }

    }
    
    /**
     * 이동 패턴 분석
     */
    private Map<String, Object> analyzeMovementPatterns(List<Map<String, Object>> positionHistory) {
        Map<String, Object> patterns = new HashMap<>();
        
        if (positionHistory.size() < 2) {
            patterns.put("totalDistance", 0);
            patterns.put("averageSpeed", 0);
            patterns.put("roamingCount", 0);
            return patterns;
        }
        
        double totalDistance = 0;
        int roamingCount = 0;
        String lastZone = "";
        
        for (int i = 1; i < positionHistory.size(); i++) {
            Map<String, Object> prev = positionHistory.get(i - 1);
            Map<String, Object> curr = positionHistory.get(i);
            
            int prevX = (Integer) prev.get("x");
            int prevY = (Integer) prev.get("y");
            int currX = (Integer) curr.get("x");
            int currY = (Integer) curr.get("y");
            
            // 거리 계산
            double distance = Math.sqrt(Math.pow(currX - prevX, 2) + Math.pow(currY - prevY, 2));
            totalDistance += distance;
            
            // 로밍 계산 (구역 변경)
            String prevZone = (String) prev.get("zone");
            String currZone = (String) curr.get("zone");
            
            if (!prevZone.equals(currZone) && !currZone.equals("unknown")) {
                roamingCount++;
            }
        }
        
        long gameDuration = positionHistory.size(); // 대략적인 게임 시간 (분)
        double averageSpeed = gameDuration > 0 ? totalDistance / gameDuration : 0;
        
        patterns.put("totalDistance", Math.round(totalDistance));
        patterns.put("averageSpeed", Math.round(averageSpeed));
        patterns.put("roamingCount", roamingCount);
        patterns.put("mobilityScore", calculateMobilityScore(totalDistance, roamingCount, gameDuration));
        
        return patterns;
    }
    
    /**
     * 위치 위험도 분석
     */
    private Map<String, Object> analyzePositionRisk(List<Map<String, Object>> positionHistory, int participantId) {
        Map<String, Object> riskAnalysis = new HashMap<>();
        
        int highRiskTime = 0; // 적 정글에 있던 시간
        int mediumRiskTime = 0; // 리버에 있던 시간
        int safeTime = 0; // 안전 지역에 있던 시간
        
        for (Map<String, Object> position : positionHistory) {
            String zone = (String) position.get("zone");
            
            switch (zone) {
                case "enemyJungle":
                    highRiskTime++;
                    break;
                case "river":
                    mediumRiskTime++;
                    break;
                case "ownJungle":
                case "topLane":
                case "midLane": 
                case "botLane":
                    safeTime++;
                    break;
            }
        }
        
        int totalTime = positionHistory.size();
        
        riskAnalysis.put("highRiskTime", highRiskTime);
        riskAnalysis.put("mediumRiskTime", mediumRiskTime);
        riskAnalysis.put("safeTime", safeTime);
        
        if (totalTime > 0) {
            riskAnalysis.put("highRiskPercentage", Math.round((highRiskTime * 100.0) / totalTime));
            riskAnalysis.put("mediumRiskPercentage", Math.round((mediumRiskTime * 100.0) / totalTime));
            riskAnalysis.put("safePercentage", Math.round((safeTime * 100.0) / totalTime));
            riskAnalysis.put("riskScore", calculateRiskScore(highRiskTime, mediumRiskTime, totalTime));
        } else {
            riskAnalysis.put("highRiskPercentage", 0);
            riskAnalysis.put("mediumRiskPercentage", 0);
            riskAnalysis.put("safePercentage", 0);
            riskAnalysis.put("riskScore", 0);
        }
        
        return riskAnalysis;
    }
    
    /**
     * 이동성 점수 계산
     */
    private Integer calculateMobilityScore(double totalDistance, int roamingCount, long gameDuration) {
        if (gameDuration == 0) return 0;
        
        // 이동성 점수 = (총 이동거리 / 게임시간) + (로밍 횟수 * 가중치)
        double distanceScore = totalDistance / gameDuration / 100; // 거리 정규화
        double roamingScore = roamingCount * 2; // 로밍 가중치
        
        int mobilityScore = (int) Math.min(100, Math.max(0, distanceScore + roamingScore));
        return mobilityScore;
    }
    
    /**
     * 위험도 점수 계산
     */
    private Integer calculateRiskScore(int highRiskTime, int mediumRiskTime, int totalTime) {
        if (totalTime == 0) return 0;
        
        // 위험도 점수 = (고위험 시간 * 3 + 중위험 시간 * 1) / 총 시간 * 100
        double riskScore = ((highRiskTime * 3.0 + mediumRiskTime * 1.0) / totalTime) * 100;
        return (int) Math.min(100, Math.max(0, riskScore));
    }

    /* 듀오 매치 비교 분석 수행 */
    public String analyzeDuoMatch(String player1Name, String player1Tag,
                                  String player2Name, String player2Tag,
                                  String matchId) {
        try {
            System.out.println("=== 듀오 매치 비교 분석 시작 ===");
            // 1. 계정 정보 조회
            AccountDto player1Account = riotService.getAccountInfo(player1Name, player1Tag);
            AccountDto player2Account = riotService.getAccountInfo(player2Name, player2Tag);

            // 2. 매치 데이터 조회 (세션 캐시 활용으로 중복 호출 방지)
            MatchDetailDto matchDetail = getMatchDetailWithCache(matchId);
            MatchTimelineDto matchTimeline = getMatchTimelineWithCache(matchId);

            // 3. 플레이어 데이터 추출
            Map<String, Object> player1Data = extractPlayerData(matchDetail, matchTimeline, player1Account.getPuuid());
            Map<String, Object> player2Data = extractPlayerData(matchDetail, matchTimeline, player2Account.getPuuid());

            // 3.5. 플레이어 랭크 정보 조회
            Map<String, Object> player1RiotInfo = riotService.getPlayerRankInfo(player1Account.getPuuid());
            Map<String, Object> player2RiotInfo = riotService.getPlayerRankInfo(player2Account.getPuuid());

            // 4. 듀오 프롬프트 생성 (컴포넌트 사용)
            String analysisPrompt = duoAnalysisPrompt.createPrompt(
                    player1Data, player2Data,
                    player1Name, player1Tag,
                    player2Name, player2Tag,
                    matchId,
                    player1RiotInfo, player2RiotInfo
            );

            // 5. AI 분석 요청
            String feedback = geminiService.sendMessage(analysisPrompt);

            System.out.println("=== 듀오 매치 비교 분석 완료 ===");
            return feedback;

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            System.err.println("듀오 매치 분석 중 오류 (Match: " + matchId + "): " + errorMessage);
            e.printStackTrace(); // 디버깅을 위한 스택 트레이스 출력
            
            // 에러 발생 시 더 구체적인 정보 포함
            StringBuilder detailedError = new StringBuilder();
            detailedError.append("듀오 분석 중 오류가 발생했습니다.\n");
            detailedError.append("Match ID: ").append(matchId).append("\n");
            detailedError.append("Player1: ").append(player1Name).append("#").append(player1Tag).append("\n");
            detailedError.append("Player2: ").append(player2Name).append("#").append(player2Tag).append("\n");
            detailedError.append("Error: ").append(errorMessage);
            
            return detailedError.toString();
        }
    }
    
    /**
     * 고급 전투 분석 데이터 생성
     */
    private Map<String, Object> createAdvancedCombatAnalysis(ParticipantDto player) {
        System.out.println("🔍 Combat Analysis Debug:");
        System.out.println("  Total Damage: " + player.getTotalDamageDealtToChampions());
        System.out.println("  Magic Damage: " + player.getMagicDamageDealtToChampions());
        System.out.println("  Physical Damage: " + player.getPhysicalDamageDealtToChampions());
        System.out.println("  True Damage: " + player.getTrueDamageDealtToChampions());
        int totalDamage = player.getTotalDamageDealtToChampions();
        
        // Debug logging to check field values
        System.out.println("=== DEBUG: Advanced Combat Analysis ===");
        System.out.println("Total Damage: " + totalDamage);
        System.out.println("Magic Damage: " + player.getMagicDamageDealtToChampions());
        System.out.println("Physical Damage: " + player.getPhysicalDamageDealtToChampions());
        System.out.println("True Damage: " + player.getTrueDamageDealtToChampions());
        
        // 피해 구성비 계산
        double magicRatio = totalDamage > 0 ? (player.getMagicDamageDealtToChampions() * 100.0) / totalDamage : 0;
        double physicalRatio = totalDamage > 0 ? (player.getPhysicalDamageDealtToChampions() * 100.0) / totalDamage : 0;
        double trueRatio = totalDamage > 0 ? (player.getTrueDamageDealtToChampions() * 100.0) / totalDamage : 0;
        
        // 전투 효율성 계산
        double damagePerGold = player.getGoldEarned() > 0 ? (double) totalDamage / player.getGoldEarned() : 0;
        double combatParticipation = player.getDeaths() > 0 ? (double) (player.getKills() + player.getAssists()) / player.getDeaths() : player.getKills() + player.getAssists();
        
        // 피해 프로필 결정
        String damageProfile;
        if (magicRatio > 60) {
            damageProfile = "AP_FOCUSED";
        } else if (physicalRatio > 60) {
            damageProfile = "AD_FOCUSED";
        } else if (trueRatio > 20) {
            damageProfile = "TRUE_DAMAGE";
        } else {
            damageProfile = "HYBRID";
        }
        
        return Map.of(
            "magicDamageRatio", Math.round(magicRatio * 10) / 10.0,
            "physicalDamageRatio", Math.round(physicalRatio * 10) / 10.0,
            "trueDamageRatio", Math.round(trueRatio * 10) / 10.0,
            "damageProfile", damageProfile,
            "damagePerGold", Math.round(damagePerGold * 100) / 100.0,
            "combatEfficiency", Math.round(combatParticipation * 100) / 100.0,
            "magicDamage", player.getMagicDamageDealtToChampions(),
            "physicalDamage", player.getPhysicalDamageDealtToChampions(),
            "trueDamage", player.getTrueDamageDealtToChampions()
        );
    }
    
    /**
     * 고급 시야 분석 데이터 생성
     */
    private Map<String, Object> createAdvancedVisionAnalysis(ParticipantDto player) {
        System.out.println("🔍 Vision Analysis Debug:");
        System.out.println("  Wards Placed: " + player.getWardsPlaced());
        System.out.println("  Wards Killed: " + player.getWardsKilled());
        System.out.println("  Control Wards: " + player.getControlWardsPlaced());
        // Debug logging to check field values
        System.out.println("=== DEBUG: Advanced Vision Analysis ===");
        System.out.println("Wards Placed: " + player.getWardsPlaced());
        System.out.println("Wards Killed: " + player.getWardsKilled());
        System.out.println("Control Wards Placed: " + player.getControlWardsPlaced());
        
        // 시야 효율성 계산
        double wardEfficiency = player.getWardsPlaced() > 0 ? (double) player.getWardsKilled() / player.getWardsPlaced() : 0;
        double controlWardRatio = player.getWardsPlaced() > 0 ? (double) player.getControlWardsPlaced() / player.getWardsPlaced() * 100 : 0;
        
        // 시야 스타일 결정
        String visionStyle;
        if (wardEfficiency > 0.5) {
            visionStyle = "AGGRESSIVE"; // 적극적 시야 파괴
        } else if (controlWardRatio > 30) {
            visionStyle = "STRATEGIC"; // 전략적 시야 운영
        } else if (player.getWardsPlaced() > 15) {
            visionStyle = "DEFENSIVE"; // 수비적 시야 확보
        } else {
            visionStyle = "PASSIVE"; // 소극적 시야 운영
        }
        
        return Map.of(
            "wardsPlaced", player.getWardsPlaced(),
            "wardsKilled", player.getWardsKilled(),
            "controlWardsPlaced", player.getControlWardsPlaced(),
            "wardEfficiency", Math.round(wardEfficiency * 1000) / 1000.0,
            "controlWardRatio", Math.round(controlWardRatio * 10) / 10.0,
            "visionStyle", visionStyle,
            "visionDominance", wardEfficiency > 0.3 ? "HIGH" : wardEfficiency > 0.1 ? "MEDIUM" : "LOW"
        );
    }
    
    /**
     * 소통 분석 데이터 생성
     */
    private Map<String, Object> createCommunicationAnalysis(ParticipantDto player) {
        System.out.println("🔍 Communication Analysis Debug:");
        System.out.println("  Danger Pings: " + player.getDangerPings());
        System.out.println("  Enemy Missing Pings: " + player.getEnemyMissingPings());
        System.out.println("  Assist Me Pings: " + player.getAssistMePings());
        // Debug logging to check field values
        System.out.println("=== DEBUG: Communication Analysis ===");
        System.out.println("All In Pings: " + player.getAllInPings());
        System.out.println("Assist Me Pings: " + player.getAssistMePings());
        System.out.println("Danger Pings: " + player.getDangerPings());
        System.out.println("Enemy Missing Pings: " + player.getEnemyMissingPings());
        
        // 총 핑 수 계산
        int totalPings = player.getAllInPings() + player.getAssistMePings() + player.getBaitPings() + 
                        player.getCommandPings() + player.getDangerPings() + player.getEnemyMissingPings() +
                        player.getEnemyVisionPings() + player.getGetBackPings() + player.getHoldPings() +
                        player.getNeedVisionPings() + player.getOnMyWayPings() + player.getPushPings() +
                        player.getVisionClearedPings();
        
        System.out.println("Total Pings: " + totalPings);
        
        // 전략적 핑 비율 (게임 이해도 관련)
        int strategicPings = player.getDangerPings() + player.getEnemyMissingPings() + 
                           player.getEnemyVisionPings() + player.getNeedVisionPings();
        double strategicRatio = totalPings > 0 ? (double) strategicPings / totalPings * 100 : 0;
        
        // 협력 핑 비율 (팀워크 관련)
        int cooperativePings = player.getAssistMePings() + player.getOnMyWayPings() + 
                              player.getHoldPings() + player.getGetBackPings();
        double cooperativeRatio = totalPings > 0 ? (double) cooperativePings / totalPings * 100 : 0;
        
        // 소통 스타일 결정
        String communicationStyle;
        if (strategicRatio > 50) {
            communicationStyle = "STRATEGIC_LEADER"; // 전략적 리더
        } else if (cooperativeRatio > 40) {
            communicationStyle = "TEAM_PLAYER"; // 팀 플레이어
        } else if (totalPings > 30) {
            communicationStyle = "ACTIVE_COMMUNICATOR"; // 적극적 소통
        } else if (totalPings > 10) {
            communicationStyle = "MODERATE_COMMUNICATOR"; // 보통 소통
        } else {
            communicationStyle = "QUIET_PLAYER"; // 조용한 플레이어
        }
        
        return Map.of(
            "totalPings", totalPings,
            "dangerPings", player.getDangerPings(),
            "enemyMissingPings", player.getEnemyMissingPings(),
            "assistMePings", player.getAssistMePings(),
            "strategicPings", strategicPings,
            "strategicRatio", Math.round(strategicRatio * 10) / 10.0,
            "cooperativeRatio", Math.round(cooperativeRatio * 10) / 10.0,
            "communicationStyle", communicationStyle,
            "gameAwareness", strategicRatio > 30 ? "HIGH" : strategicRatio > 15 ? "MEDIUM" : "LOW"
        );
    }
    
    /**
     * 고급 분석 프롬프트 섹션 생성
     */
    private String createAdvancedAnalysisPrompt(Map<String, Object> playerData) {
        StringBuilder prompt = new StringBuilder();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> advancedCombat = (Map<String, Object>) playerData.get("advancedCombat");
        @SuppressWarnings("unchecked")
        Map<String, Object> advancedVision = (Map<String, Object>) playerData.get("advancedVision");
        @SuppressWarnings("unchecked")
        Map<String, Object> communicationData = (Map<String, Object>) playerData.get("communicationData");
        
        // 전투 효율성 분석
        if (advancedCombat != null) {
            prompt.append("=== 전투 효율성 및 빌드 분석 ===\n");
            prompt.append("피해 구성: 마법 ").append(advancedCombat.get("magicDamageRatio")).append("% / ")
                  .append("물리 ").append(advancedCombat.get("physicalDamageRatio")).append("% / ")
                  .append("고정 ").append(advancedCombat.get("trueDamageRatio")).append("%\n");
            prompt.append("빌드 유형: ").append(getDamageProfileDescription((String) advancedCombat.get("damageProfile"))).append("\n");
            prompt.append("골드 대비 딜량 효율: ").append(advancedCombat.get("damagePerGold")).append("\n");
            prompt.append("전투 참여 효율: ").append(advancedCombat.get("combatEfficiency")).append("\n\n");
        }
        
        // 시야 운영 분석
        if (advancedVision != null) {
            prompt.append("=== 시야 운영 및 맵 장악력 ===\n");
            prompt.append("와드 설치: ").append(advancedVision.get("wardsPlaced")).append("개 / ")
                  .append("제거: ").append(advancedVision.get("wardsKilled")).append("개\n");
            prompt.append("제어 와드: ").append(advancedVision.get("controlWardsPlaced")).append("개\n");
            prompt.append("시야 운영 스타일: ").append(getVisionStyleDescription((String) advancedVision.get("visionStyle"))).append("\n");
            prompt.append("시야 지배력: ").append(advancedVision.get("visionDominance")).append("\n\n");
        }
        
        // 소통 및 게임 이해도 분석
        if (communicationData != null) {
            prompt.append("=== 소통 능력 및 게임 이해도 ===\n");
            prompt.append("총 핑 사용: ").append(communicationData.get("totalPings")).append("회\n");
            prompt.append("위험 알림: ").append(communicationData.get("dangerPings")).append("회 / ")
                  .append("적 실종: ").append(communicationData.get("enemyMissingPings")).append("회\n");
            prompt.append("도움 요청: ").append(communicationData.get("assistMePings")).append("회\n");
            prompt.append("소통 스타일: ").append(getCommunicationStyleDescription((String) communicationData.get("communicationStyle"))).append("\n");
            prompt.append("게임 이해도: ").append(communicationData.get("gameAwareness")).append("\n\n");
        }
        
        return prompt.toString();
    }
    
    /**
     * 피해 프로필 설명 반환
     */
    private String getDamageProfileDescription(String profile) {
        switch (profile) {
            case "AP_FOCUSED": return "마법 딜러 (AP 위주)";
            case "AD_FOCUSED": return "물리 딜러 (AD 위주)";
            case "TRUE_DAMAGE": return "고정 피해 딜러";
            case "HYBRID": return "하이브리드 딜러";
            default: return "혼합형";
        }
    }
    
    /**
     * 시야 스타일 설명 반환
     */
    private String getVisionStyleDescription(String style) {
        switch (style) {
            case "AGGRESSIVE": return "공격적 시야 장악";
            case "STRATEGIC": return "전략적 시야 운영";
            case "DEFENSIVE": return "수비적 시야 확보";
            case "PASSIVE": return "소극적 시야 운영";
            default: return "일반적 시야 운영";
        }
    }
    
    /**
     * 소통 스타일 설명 반환
     */
    private String getCommunicationStyleDescription(String style) {
        switch (style) {
            case "STRATEGIC_LEADER": return "전략적 리더 (팀 지휘)";
            case "TEAM_PLAYER": return "협력적 팀원";
            case "ACTIVE_COMMUNICATOR": return "적극적 소통형";
            case "MODERATE_COMMUNICATOR": return "보통 소통형";
            case "QUIET_PLAYER": return "조용한 플레이어";
            default: return "일반적 소통";
        }
    }
    
    /**
     * 포지션 기반으로 상대팀에서 같은 포지션의 플레이어 찾기
     */
    private ParticipantDto findOpponentByPosition(MatchDetailDto matchDetail, String position, int playerTeamId) {
        if (position == null || position.isEmpty()) {
            return null;
        }
        
        return matchDetail.getInfo().getParticipants().stream()
                .filter(p -> position.equals(p.getTeamPosition())) // 같은 포지션
                .filter(p -> p.getTeamId() != playerTeamId)        // 상대팀
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 시간별 위치 데이터와 주요 이벤트가 포함된 플레이어 이벤트 추출
     * 원래 시간별 위치 데이터 시스템을 복원하여 분석 품질 향상
     */
    private List<Map<String, Object>> extractBasicPlayerEvents(MatchTimelineDto matchTimeline, int participantId) {
        List<Map<String, Object>> playerEvents = new ArrayList<>();
        
        if (matchTimeline == null || matchTimeline.getInfo() == null || matchTimeline.getInfo().getFrames() == null) {
            return playerEvents;
        }
        
        System.out.println("=== 시간별 위치 데이터 추출 시작 ===");
        System.out.println("참가자 ID: " + participantId);
        
        // 1. 시간별 위치 데이터 추출 (participantFrames 활용)
        List<Map<String, Object>> positionHistory = new ArrayList<>();
        
        for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
            long timestampMs = frame.getTimestamp();
            int minutes = (int) (timestampMs / 60000);
            int seconds = (int) ((timestampMs % 60000) / 1000);
            String timeString = String.format("%d:%02d", minutes, seconds);
            
            if (frame.getParticipantFrames() != null) {
                // participantId를 String으로 변환하여 프레임 조회
                ParticipantFrameDto playerFrame = frame.getParticipantFrames().get(String.valueOf(participantId));
                
                if (playerFrame != null && playerFrame.getPosition() != null) {
                    Map<String, Object> positionData = new HashMap<>();
                    positionData.put("timestamp", timestampMs);
                    positionData.put("time", timeString);
                    positionData.put("timeMinutes", String.format("%.1f분", timestampMs / 60000.0));
                    positionData.put("type", "POSITION");
                    positionData.put("x", playerFrame.getPosition().getX());
                    positionData.put("y", playerFrame.getPosition().getY());
                    positionData.put("location", determineEventLocation(playerFrame.getPosition()));
                    positionData.put("level", playerFrame.getLevel());
                    positionData.put("gold", playerFrame.getTotalGold());
                    positionData.put("minionsKilled", playerFrame.getMinionsKilled());
                    positionData.put("jungleMinionsKilled", playerFrame.getJungleMinionsKilled());
                    positionData.put("description", String.format("위치: %s (레벨 %d, 골드 %d)", 
                        determineEventLocation(playerFrame.getPosition()), 
                        playerFrame.getLevel(), 
                        playerFrame.getTotalGold()));
                    
                    positionHistory.add(positionData);
                }
            }
        }
        
        System.out.println("추출된 위치 데이터 개수: " + positionHistory.size());
        
        // 2. 킬/데스 이벤트 추출
        List<Map<String, Object>> combatEvents = new ArrayList<>();
        
        for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
            if (frame.getEvents() != null) {
                for (EventDto event : frame.getEvents()) {
                    if ("CHAMPION_KILL".equals(event.getType()) && 
                        (participantId == event.getKillerId() || participantId == event.getVictimId())) {
                        
                        Map<String, Object> eventData = new HashMap<>();
                        
                        // 시간 정보 (밀리초 → 분:초 형식)
                        long timestampMs = event.getTimestamp();
                        int minutes = (int) (timestampMs / 60000);
                        int seconds = (int) ((timestampMs % 60000) / 1000);
                        String timeString = String.format("%d:%02d", minutes, seconds);
                        
                        // 위치 정보 (null 안전성 체크)
                        String location = event.getPosition() != null ? 
                            determineEventLocation(event.getPosition()) : "알 수 없는 위치";
                        
                        // 이벤트 상세 정보
                        String description;
                        if (participantId == event.getKillerId()) {
                            description = String.format("킬 획득 (%s)", location);
                        } else {
                            description = String.format("데스 (%s)", location);
                        }
                        
                        eventData.put("timestamp", timestampMs);
                        eventData.put("time", timeString);
                        eventData.put("timeMinutes", String.format("%.1f분", timestampMs / 60000.0));
                        eventData.put("type", event.getType());
                        eventData.put("location", location);
                        eventData.put("description", description);
                        
                        if (event.getPosition() != null) {
                            eventData.put("x", event.getPosition().getX());
                            eventData.put("y", event.getPosition().getY());
                        }
                        
                        combatEvents.add(eventData);
                    }
                }
            }
        }
        
        System.out.println("추출된 전투 이벤트 개수: " + combatEvents.size());
        
        // 3. 위치 데이터와 이벤트 데이터 통합
        playerEvents.addAll(positionHistory);
        playerEvents.addAll(combatEvents);
        
        // 4. 시간순 정렬
        playerEvents.sort((e1, e2) -> 
            Long.compare((Long)e1.get("timestamp"), (Long)e2.get("timestamp"))
        );
        
        System.out.println("총 통합 이벤트 개수: " + playerEvents.size());
        System.out.println("=== 시간별 위치 데이터 추출 완료 ===");
        
        return playerEvents;
    }
    
    /**
     * 이벤트 위치를 기반으로 맵 구역 판단
     */
    private String determineEventLocation(PositionDto position) {
        if (position == null) {
            return "알 수 없는 위치";
        }
        
        int x = position.getX();
        int y = position.getY();
        
        // 소환사의 협곡 주요 구역 판단 (대략적인 좌표)
        if (x < 7000 && y > 9000) return "탑 라인";
        if (x > 7000 && x < 9000 && y > 7000 && y < 9000) return "미드 라인";  
        if (x > 9000 && y < 7000) return "봇 라인";
        if (x < 7000 && y < 7000) return "정글 (하단)";
        if (x < 7000 && y > 7000) return "정글 (상단)";
        if (x > 9000 && y > 9000) return "정글 (우상단)";
        if (x > 9000 && y > 7000 && y < 9000) return "정글 (우측)";
        if (x > 7000 && x < 9000 && y < 7000) return "정글 (하중앙)";
        if (x > 7000 && x < 9000 && y > 9000) return "정글 (상중앙)";
        
        return "맵 중앙";
    }

}