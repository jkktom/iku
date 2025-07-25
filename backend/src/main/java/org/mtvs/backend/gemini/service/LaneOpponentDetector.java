package org.mtvs.backend.gemini.service;

import org.mtvs.backend.analysis.analyzer.MapAnalyzer;
import org.mtvs.backend.gemini.dto.OpponentData;
import org.mtvs.backend.riot.dto.MatchDetailDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.dto.ParticipantDto;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 라인 상대 탐지 로직 - 포지셔닝 데이터를 기반으로 직접 상대를 찾음
 */
@Component
public class LaneOpponentDetector {
    
    private final List<MapAnalyzer> mapAnalyzers;
    
    public LaneOpponentDetector(List<MapAnalyzer> mapAnalyzers) {
        this.mapAnalyzers = mapAnalyzers;
    }
    
    /**
     * 특정 플레이어의 라인 상대를 탐지
     * @param matchDetail 매치 상세 정보
     * @param matchTimeline 매치 타임라인
     * @param playerParticipantId 분석 대상 플레이어 ID
     * @return 라인 상대 데이터 (없으면 null)
     */
    public OpponentData detectLaneOpponent(MatchDetailDto matchDetail, MatchTimelineDto matchTimeline, int playerParticipantId) {
        try {
            System.out.println("  🔍 라인 상대 탐지 로직 시작 (ParticipantId: " + playerParticipantId + ")");
            
            // 1. 맵 분석기 선택
            MapAnalyzer analyzer = getMapAnalyzer(matchDetail.getInfo().getMapId());
            if (analyzer == null) {
                System.out.println("  ❌ 지원하지 않는 맵 ID: " + matchDetail.getInfo().getMapId());
                return null;
            }
            System.out.println("  ✅ 맵 분석기 선택됨: " + analyzer.getMapName() + " (ID: " + matchDetail.getInfo().getMapId() + ")");
            
            // 1.1. 칼바람 나락에서는 라인 상대 탐지 불필요
            if (matchDetail.getInfo().getMapId() == 12) {
                System.out.println("  ⏭️ 칼바람 나락에서는 라인 상대 탐지를 하지 않음");
                return null;
            }
            
            // 2. 플레이어 정보 획득
            ParticipantDto player = findParticipant(matchDetail, playerParticipantId);
            if (player == null) {
                System.out.println("  ❌ 플레이어를 찾을 수 없음 (ParticipantId: " + playerParticipantId + ")");
                return null;
            }
            System.out.println("  ✅ 플레이어 발견: " + player.getChampionName() + " (팀: " + player.getTeamId() + ")");
            
            // 3. 플레이어의 주요 활동 구역 분석
            System.out.println("  📍 플레이어 위치 분석 중...");
            Map<String, Object> playerPositionAnalysis = analyzer.analyzePlayerPosition(matchTimeline, playerParticipantId, player.getTeamId());
            String primaryLane = determinePrimaryLane(playerPositionAnalysis);
            
            System.out.println("  📍 플레이어 주요 라인: " + primaryLane);
            if (playerPositionAnalysis.containsKey("zoneTimeSpent")) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> zoneTime = (Map<String, Integer>) playerPositionAnalysis.get("zoneTimeSpent");
                System.out.println("    구역별 활동: " + zoneTime);
            }
            
            // 4. 적팀에서 같은 라인에서 활동한 플레이어 찾기
            List<ParticipantDto> enemyTeam = getEnemyTeamParticipants(matchDetail, player.getTeamId());
            System.out.println("  👥 적팀 구성원 수: " + enemyTeam.size());
            
            ParticipantDto laneOpponent = null;
            double maxOverlap = 0.0;
            
            System.out.println("  🔎 라인 매칭 분석:");
            for (ParticipantDto enemy : enemyTeam) {
                Map<String, Object> enemyPositionAnalysis = analyzer.analyzePlayerPosition(matchTimeline, enemy.getParticipantId(), enemy.getTeamId());
                String enemyPrimaryLane = determinePrimaryLane(enemyPositionAnalysis);
                
                System.out.println("    적 " + enemy.getChampionName() + " (ID: " + enemy.getParticipantId() + ") - 주요 라인: " + enemyPrimaryLane);
                
                // 같은 라인에서 활동한 경우
                if (primaryLane.equals(enemyPrimaryLane)) {
                    double laneOverlap = calculateLaneOverlap(playerPositionAnalysis, enemyPositionAnalysis, primaryLane);
                    
                    System.out.println("      💥 같은 라인 활동! 겹침도: " + String.format("%.3f", laneOverlap));
                    
                    if (laneOverlap > maxOverlap) {
                        maxOverlap = laneOverlap;
                        laneOpponent = enemy;
                        System.out.println("      🎯 새로운 최고 후보: " + enemy.getChampionName());
                    }
                } else {
                    System.out.println("      ⏭️ 다른 라인 (" + primaryLane + " vs " + enemyPrimaryLane + ")");
                }
            }
            
            // 5. 라인 상대가 발견되면 OpponentData 생성
            System.out.println("  🎯 최종 결과: 최대 겹침도 = " + String.format("%.3f", maxOverlap) + ", 기준: 0.3");
            if (laneOpponent != null && maxOverlap > 0.3) { // 30% 이상 겹쳐야 라인 상대로 인정
                System.out.println("  ✅ 라인 상대 확정: " + laneOpponent.getChampionName() + " (겹침: " + String.format("%.3f", maxOverlap) + ")");
                return createOpponentData(player, laneOpponent, matchDetail, matchTimeline);
            }
            
            if (laneOpponent != null) {
                System.out.println("  ⚠️ 겹침도 부족으로 라인 상대 제외: " + laneOpponent.getChampionName() + " (겹침: " + String.format("%.3f", maxOverlap) + " < 0.3)");
            } else {
                System.out.println("  ❌ 같은 라인에서 활동한 적이 없음");
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("  💥 라인 상대 탐지 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 맵 ID에 맞는 분석기 선택
     */
    private MapAnalyzer getMapAnalyzer(int mapId) {
        return mapAnalyzers.stream()
                .filter(analyzer -> analyzer.getSupportedMapId() == mapId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 참가자 정보 찾기
     */
    private ParticipantDto findParticipant(MatchDetailDto matchDetail, int participantId) {
        return matchDetail.getInfo().getParticipants().stream()
                .filter(p -> p.getParticipantId() == participantId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 주요 활동 라인 결정 (가장 오래 머문 라인)
     */
    @SuppressWarnings("unchecked")
    private String determinePrimaryLane(Map<String, Object> positionAnalysis) {
        Map<String, Integer> zoneTimeSpent = (Map<String, Integer>) positionAnalysis.get("zoneTimeSpent");
        
        if (zoneTimeSpent == null) return "unknown";
        
        // 라인 구역만 고려 (정글, 강 제외)
        List<String> laneZones = Arrays.asList("topLane", "midLane", "botLane");
        
        return zoneTimeSpent.entrySet().stream()
                .filter(entry -> laneZones.contains(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }
    
    /**
     * 적팀 참가자들 반환
     */
    private List<ParticipantDto> getEnemyTeamParticipants(MatchDetailDto matchDetail, int playerTeamId) {
        return matchDetail.getInfo().getParticipants().stream()
                .filter(p -> p.getTeamId() != playerTeamId)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 라인에서의 활동 겹침 정도 계산
     */
    @SuppressWarnings("unchecked")
    private double calculateLaneOverlap(Map<String, Object> playerAnalysis, Map<String, Object> enemyAnalysis, String lane) {
        Map<String, Integer> playerZoneTime = (Map<String, Integer>) playerAnalysis.get("zoneTimeSpent");
        Map<String, Integer> enemyZoneTime = (Map<String, Integer>) enemyAnalysis.get("zoneTimeSpent");
        
        if (playerZoneTime == null || enemyZoneTime == null) return 0.0;
        
        int playerLaneTime = playerZoneTime.getOrDefault(lane, 0);
        int enemyLaneTime = enemyZoneTime.getOrDefault(lane, 0);
        
        if (playerLaneTime == 0 && enemyLaneTime == 0) return 0.0;
        
        // 겹침 정도 = min(두 플레이어의 라인 시간) / max(두 플레이어의 라인 시간)
        int minTime = Math.min(playerLaneTime, enemyLaneTime);
        int maxTime = Math.max(playerLaneTime, enemyLaneTime);
        
        return maxTime > 0 ? (double) minTime / maxTime : 0.0;
    }
    
    /**
     * OpponentData 객체 생성 (비교 지표 포함)
     */
    private OpponentData createOpponentData(ParticipantDto player, ParticipantDto opponent, 
                                          MatchDetailDto matchDetail, MatchTimelineDto matchTimeline) {
        
        OpponentData opponentData = new OpponentData(
            opponent.getChampionName(),
            opponent.getRiotIdGameName(),
            opponent.getParticipantId(),
            opponent.getTeamId()
        );
        
        // 기본 통계
        opponentData.setKills(opponent.getKills());
        opponentData.setDeaths(opponent.getDeaths());
        opponentData.setAssists(opponent.getAssists());
        opponentData.setTotalCS(opponent.getTotalMinionsKilled() + opponent.getNeutralMinionsKilled());
        opponentData.setGoldEarned(opponent.getGoldEarned());
        
        // 비교 지표 계산
        int playerCS = player.getTotalMinionsKilled() + player.getNeutralMinionsKilled();
        int opponentCS = opponentData.getTotalCS();
        opponentData.setCsDifference(opponentCS - playerCS);
        opponentData.setGoldDifference(opponent.getGoldEarned() - player.getGoldEarned());
        
        // 라인 결과 판정
        String laneResult = determineLaneResult(player, opponent);
        opponentData.setLaneResult(laneResult);
        
        // 상호 킬/데스 계산 (타임라인 분석 필요)
        calculateHeadToHeadStats(opponentData, player.getParticipantId(), opponent.getParticipantId(), matchTimeline);
        
        return opponentData;
    }
    
    /**
     * 라인전 결과 판정
     */
    private String determineLaneResult(ParticipantDto player, ParticipantDto opponent) {
        int playerCS = player.getTotalMinionsKilled() + player.getNeutralMinionsKilled();
        int opponentCS = opponent.getTotalMinionsKilled() + opponent.getNeutralMinionsKilled();
        
        int csDiff = playerCS - opponentCS;
        int goldDiff = player.getGoldEarned() - opponent.getGoldEarned();
        int kdaDiff = (player.getKills() + player.getAssists()) - (opponent.getKills() + opponent.getAssists());
        
        // 종합 점수 계산 (CS 50%, 골드 30%, KDA 20%)
        double score = (csDiff * 0.5) + (goldDiff * 0.0003) + (kdaDiff * 0.2);
        
        if (score > 0.5) return "won";
        else if (score < -0.5) return "lost";
        else return "even";
    }
    
    /**
     * 1:1 킬/데스 통계 계산
     */
    private void calculateHeadToHeadStats(OpponentData opponentData, int playerId, int opponentId, MatchTimelineDto matchTimeline) {
        int killsAgainstPlayer = 0;
        int deathsToPlayer = 0;
        
        if (matchTimeline.getInfo() != null && matchTimeline.getInfo().getFrames() != null) {
            matchTimeline.getInfo().getFrames().forEach(frame -> {
                if (frame.getEvents() != null) {
                    frame.getEvents().stream()
                        .filter(event -> "CHAMPION_KILL".equals(event.getType()))
                        .forEach(event -> {
                            // 상대가 플레이어를 킬한 경우
                            if (Objects.equals(event.getKillerId(), opponentId) && 
                                Objects.equals(event.getVictimId(), playerId)) {
                                opponentData.setKillsAgainstPlayer(opponentData.getKillsAgainstPlayer() + 1);
                            }
                            // 플레이어가 상대를 킬한 경우
                            else if (Objects.equals(event.getKillerId(), playerId) && 
                                     Objects.equals(event.getVictimId(), opponentId)) {
                                opponentData.setDeathsToPlayer(opponentData.getDeathsToPlayer() + 1);
                            }
                        });
                }
            });
        }
    }
}