package org.mtvs.backend.gemini.service;

import org.mtvs.backend.riot.dto.*;
import org.mtvs.backend.riot.service.RiotService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameAnalysisService {
    
    private final RiotService riotService;
    private final GeminiService geminiService;
    
    public GameAnalysisService(RiotService riotService, GeminiService geminiService) {
        this.riotService = riotService;
        this.geminiService = geminiService;
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
     * 특정 매치에서 특정 플레이어의 상세 분석 수행
     */
    public String analyzePlayerMatch(String gameName, String tagLine, String matchId) {
        try {
            System.out.println("=== 개인 매치 분석 시작 ===");
            System.out.println("플레이어: " + gameName + "#" + tagLine);
            System.out.println("매치 ID: " + matchId);
            
            // 1. 계정 정보 조회
            AccountDto account = riotService.getAccountInfo(gameName, tagLine);
            
            // 2. 매치 상세 정보 및 타임라인 조회
            MatchDetailDto matchDetail = riotService.getMatchDetail(matchId);
            MatchTimelineDto matchTimeline = riotService.getMatchTimeline(matchId);
            
            // 3. 특정 플레이어 데이터 추출
            Map<String, Object> playerData = extractPlayerData(matchDetail, matchTimeline, account.getPuuid());
            
            // 4. 개인 분석용 프롬프트 생성
            String analysisPrompt = createPersonalAnalysisPrompt(playerData, matchId);
            
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
                
        if (player == null) {
            throw new RuntimeException("플레이어 데이터를 찾을 수 없습니다");
        }
        
        // 2. 기본 정보
        playerData.put("playerInfo", Map.of(
            "summonerName", player.getRiotIdGameName(),
            "championName", player.getChampionName(),
            "participantId", player.getParticipantId(),
            "teamId", player.getTeamId(),
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
        
        // 3. 최종 스탯
        playerData.put("finalStats", Map.of(
            "kills", player.getKills(),
            "deaths", player.getDeaths(),
            "assists", player.getAssists(),
            "totalCS", player.getTotalMinionsKilled() + player.getNeutralMinionsKilled(),
            "goldEarned", player.getGoldEarned(),
            "damageDealt", player.getTotalDamageDealtToChampions(),
            "damageTaken", player.getTotalDamageTaken(),
            "visionScore", player.getVisionScore()
        ));
        
        // 4. 게임 정보
        playerData.put("gameInfo", Map.of(
            "duration", matchDetail.getInfo().getGameDuration(),
            "gameMode", matchDetail.getInfo().getGameMode(),
            "queueType", riotService.getQueueName(matchDetail.getInfo().getQueueId())
        ));
        
        // 5. 타임라인 이벤트 추출 (올바른 participantId 사용)
        List<Map<String, Object>> playerEvents = extractPlayerEvents(matchTimeline, correctParticipantId);
        playerData.put("timelineEvents", playerEvents);
        
        // 6. 위치 정보 기반 분석 추가
        Map<String, Object> positionAnalysis = extractPositionAnalysis(matchTimeline, correctParticipantId);
        playerData.put("positionAnalysis", positionAnalysis);
        
        return playerData;
    }
    
    /**
     * 특정 플레이어 관련 타임라인 이벤트 추출
     */
    private List<Map<String, Object>> extractPlayerEvents(MatchTimelineDto matchTimeline, int participantId) {
        List<Map<String, Object>> playerEvents = new ArrayList<>();
        
        if (matchTimeline.getInfo() != null && matchTimeline.getInfo().getFrames() != null) {
            for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
                if (frame.getEvents() != null) {
                    for (EventDto event : frame.getEvents()) {
                        // 해당 플레이어와 관련된 이벤트만 추출
                        if (isPlayerRelatedEvent(event, participantId)) {
                            Map<String, Object> eventData = new HashMap<>();
                            eventData.put("type", event.getType());
                            eventData.put("timestamp", event.getTimestamp());
                            eventData.put("timeMinutes", String.format("%.1f분", event.getTimestamp() / 60000.0));
                            eventData.put("participantId", event.getParticipantId());
                            eventData.put("killerId", event.getKillerId());
                            eventData.put("victimId", event.getVictimId());
                            eventData.put("assistingParticipantIds", event.getAssistingParticipantIds());
                            eventData.put("monsterType", event.getMonsterType());
                            eventData.put("buildingType", event.getBuildingType());
                            eventData.put("description", createEventDescription(event, participantId));
                            
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
                return timeStr + ": " + event.getMonsterType() + " 처치 참여";
                
            case "BUILDING_KILL":
                return timeStr + ": " + event.getBuildingType() + " 파괴 참여";
                
            case "ITEM_PURCHASED":
                return timeStr + ": 아이템 구매 (ID: " + event.getItemId() + ")";
                
            case "LEVEL_UP":
                return timeStr + ": 레벨업";
                
            default:
                return timeStr + ": " + event.getType();
        }
        
        return timeStr + ": " + event.getType();
    }
    
    /**
     * 개인 분석용 프롬프트 생성
     */
    private String createPersonalAnalysisPrompt(Map<String, Object> playerData, String matchId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> playerInfo = (Map<String, Object>) playerData.get("playerInfo");
        @SuppressWarnings("unchecked")
        Map<String, Object> finalStats = (Map<String, Object>) playerData.get("finalStats");
        @SuppressWarnings("unchecked")
        Map<String, Object> gameInfo = (Map<String, Object>) playerData.get("gameInfo");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) playerData.get("timelineEvents");
        @SuppressWarnings("unchecked")
        Map<String, Object> positionAnalysis = (Map<String, Object>) playerData.get("positionAnalysis");
        
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
        
        // 게임 흐름 (주요 이벤트)
        prompt.append("=== 게임 흐름 및 주요 이벤트 ===\n");
        for (Map<String, Object> event : events) {
            prompt.append(event.get("description")).append("\n");
        }
        prompt.append("\n");
        
        // 위치 및 포지셔닝 분석
        if (positionAnalysis != null) {
            @SuppressWarnings("unchecked")
            Integer totalRecords = (Integer) positionAnalysis.get("totalPositionRecords");
            
            if (totalRecords != null && totalRecords > 0) {
                prompt.append("=== 포지셔닝 및 이동 패턴 분석 ===\n");
                
                @SuppressWarnings("unchecked")
                Map<String, Integer> zoneTimeSpent = (Map<String, Integer>) positionAnalysis.get("zoneTimeSpent");
                @SuppressWarnings("unchecked")
                Map<String, Object> movementPatterns = (Map<String, Object>) positionAnalysis.get("movementPatterns");
                @SuppressWarnings("unchecked")
                Map<String, Object> riskAnalysis = (Map<String, Object>) positionAnalysis.get("riskAnalysis");
                
                if (zoneTimeSpent != null) {
                    prompt.append("맵 구역별 활동 시간:\n");
                    prompt.append("- 자팀 정글: ").append(zoneTimeSpent.get("ownJungle")).append("분\n");
                    prompt.append("- 적팀 정글: ").append(zoneTimeSpent.get("enemyJungle")).append("분\n");
                    prompt.append("- 탑 레인: ").append(zoneTimeSpent.get("topLane")).append("분\n");
                    prompt.append("- 미드 레인: ").append(zoneTimeSpent.get("midLane")).append("분\n");
                    prompt.append("- 봇 레인: ").append(zoneTimeSpent.get("botLane")).append("분\n");
                    prompt.append("- 리버: ").append(zoneTimeSpent.get("river")).append("분\n");
                }
                
                if (movementPatterns != null) {
                    prompt.append("이동 패턴 분석:\n");
                    prompt.append("- 총 이동거리: ").append(movementPatterns.get("totalDistance")).append(" 유닛\n");
                    prompt.append("- 평균 이동속도: ").append(movementPatterns.get("averageSpeed")).append(" 유닛/분\n");
                    prompt.append("- 로밍 횟수: ").append(movementPatterns.get("roamingCount")).append("회\n");
                    prompt.append("- 이동성 점수: ").append(movementPatterns.get("mobilityScore")).append("/100\n");
                }
                
                if (riskAnalysis != null) {
                    prompt.append("위험도 분석:\n");
                    prompt.append("- 고위험 지역 체류: ").append(riskAnalysis.get("highRiskPercentage")).append("%\n");
                    prompt.append("- 중위험 지역 체류: ").append(riskAnalysis.get("mediumRiskPercentage")).append("%\n");
                    prompt.append("- 안전 지역 체류: ").append(riskAnalysis.get("safePercentage")).append("%\n");
                    prompt.append("- 위험도 점수: ").append(riskAnalysis.get("riskScore")).append("/100\n");
                }
                prompt.append("\n");
            } else {
                prompt.append("=== 포지셔닝 정보 ===\n");
                prompt.append("이 게임에서는 위치 정보가 제공되지 않아 포지셔닝 분석을 수행할 수 없습니다.\n");
                prompt.append("대신 타임라인 이벤트를 바탕으로 플레이 패턴을 분석하겠습니다.\n\n");
            }
        }
        
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
     * 플레이어의 위치 정보 기반 분석
     */
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
        
        // 상단 (탑 레인)
        if (y > 10000) {
            return "topLane";
        }
        
        // 하단 (봇 레인)  
        if (y < 5000) {
            return "botLane";
        }
        
        // 중앙 (미드 레인)
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
}