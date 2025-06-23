package org.mtvs.backend.gemini.service;

import org.mtvs.backend.riot.dto.AccountDto;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
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
        MatchDetailDto.Participant player = matchDetail.getInfo().getParticipants().stream()
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
        MatchDetailDto.Participant player = matchDetail.getInfo().getParticipants().stream()
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
            "participantId", player.getParticipandId(),
            "teamId", player.getTeamId(),
            "result", player.isWin() ? "승리" : "패배"
        ));
        
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
        
        // 5. 타임라인 이벤트 추출 (해당 플레이어 관련만)
        List<Map<String, Object>> playerEvents = extractPlayerEvents(matchTimeline, player.getParticipandId());
        playerData.put("timelineEvents", playerEvents);
        
        return playerData;
    }
    
    /**
     * 특정 플레이어 관련 타임라인 이벤트 추출
     */
    private List<Map<String, Object>> extractPlayerEvents(MatchTimelineDto matchTimeline, int participantId) {
        List<Map<String, Object>> playerEvents = new ArrayList<>();
        
        if (matchTimeline.getInfo() != null && matchTimeline.getInfo().getFrames() != null) {
            for (MatchTimelineDto.Frame frame : matchTimeline.getInfo().getFrames()) {
                if (frame.getEvents() != null) {
                    for (MatchTimelineDto.Event event : frame.getEvents()) {
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
    private boolean isPlayerRelatedEvent(MatchTimelineDto.Event event, int participantId) {
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
    private String createEventDescription(MatchTimelineDto.Event event, int participantId) {
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
        
        prompt.append("4. **구체적인 개선 가이드**\n");
        prompt.append("   - 즉시 적용 가능한 개선점 3가지\n");
        prompt.append("   - 다음 게임에서 집중할 포인트\n");
        prompt.append("   - 이 챔피언으로 더 잘하는 방법\n\n");
        
        prompt.append("각 분석은 구체적인 시간대와 상황을 언급하며, 실행 가능한 조언으로 제공해주세요.\n");
        prompt.append("전체적인 평가와 함께 가장 개선이 필요한 부분을 우선순위로 제시해주세요.");
        
        return prompt.toString();
    }
}