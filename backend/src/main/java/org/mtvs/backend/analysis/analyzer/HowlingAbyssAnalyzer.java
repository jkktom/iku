package org.mtvs.backend.analysis.analyzer;

import org.mtvs.backend.riot.dto.FrameDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.dto.ParticipantFrameDto;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 칼바람 나락 (Map ID: 12) 전용 분석기  
 * ARAM 모드: 단일 라인, 포지셔닝과 교전 위치가 핵심
 */
@Component
public class HowlingAbyssAnalyzer implements MapAnalyzer {
    
    private static final int MAP_ID = 12;
    private static final String MAP_NAME = "칼바람 나락";
    
    // ARAM에서 중요한 이벤트 타입들 (정글 몬스터 제외)
    private static final Set<String> IMPORTANT_EVENTS = Set.of(
        "CHAMPION_KILL",
        "BUILDING_KILL"  // 정글 몬스터는 ARAM에 없음
    );
    
    @Override
    public int getSupportedMapId() {
        return MAP_ID;
    }
    
    @Override
    public String getMapName() {
        return MAP_NAME;
    }
    
    @Override
    public Map<String, Object> analyzePlayerPosition(MatchTimelineDto matchTimeline, int participantId, int teamId) {
        List<Map<String, Object>> positionHistory = new ArrayList<>();
        Map<String, Integer> zoneTimeSpent = new HashMap<>();
        
        // ARAM 구역별 시간 초기화
        zoneTimeSpent.put("friendlyZone", 0);    // 아군 구역
        zoneTimeSpent.put("neutralZone", 0);     // 중앙 구역  
        zoneTimeSpent.put("enemyZone", 0);       // 적군 구역
        zoneTimeSpent.put("dangerZone", 0);      // 위험 구역 (적 타워 근처)
        
        double totalDistance = 0.0;
        int frameCount = 0;
        int aggressivePositions = 0;  // 공격적 포지셔닝 횟수
        String previousZone = "";
        
        Map<String, Object> previousPosition = null;
        
        for (FrameDto frame : matchTimeline.getInfo().getFrames()) {
            if (frame.getParticipantFrames() != null && 
                frame.getParticipantFrames().containsKey(String.valueOf(participantId))) {
                
                ParticipantFrameDto participantFrame = frame.getParticipantFrames().get(String.valueOf(participantId));
                
                if (participantFrame.getPosition() != null) {
                    int x = participantFrame.getPosition().getX();
                    int y = participantFrame.getPosition().getY();
                    long timestamp = frame.getTimestamp();
                    
                    // 위치 기록 생성
                    Map<String, Object> position = new HashMap<>();
                    position.put("timestamp", timestamp);
                    position.put("timeMinutes", String.format("%.1f분", timestamp / 60000.0));
                    position.put("x", x);
                    position.put("y", y);
                    position.put("level", participantFrame.getLevel());
                    position.put("totalGold", participantFrame.getTotalGold());
                    
                    String zone = determineMapZone(x, y, teamId);
                    String riskLevel = evaluateRiskLevel(x, y, teamId);
                    position.put("zone", zone);
                    position.put("riskLevel", riskLevel);
                    
                    positionHistory.add(position);
                    
                    // 구역 체류 시간 계산
                    zoneTimeSpent.put(zone, zoneTimeSpent.get(zone) + 1);
                    
                    // 공격적 포지셔닝 카운트
                    if ("enemyZone".equals(zone) || "dangerZone".equals(zone)) {
                        aggressivePositions++;
                    }
                    
                    // 이동 거리 계산
                    if (previousPosition != null) {
                        int prevX = (Integer) previousPosition.get("x");
                        int prevY = (Integer) previousPosition.get("y");
                        double distance = Math.sqrt(Math.pow(x - prevX, 2) + Math.pow(y - prevY, 2));
                        totalDistance += distance;
                    }
                    
                    previousPosition = position;
                    frameCount++;
                }
            }
        }
        
        // 위험도 분석
        Map<String, Object> riskAnalysis = calculateRiskAnalysis(positionHistory);
        
        // ARAM 전용 이동 패턴 분석
        Map<String, Object> movementPatterns = new HashMap<>();
        movementPatterns.put("totalDistance", totalDistance);
        movementPatterns.put("averageSpeed", frameCount > 0 ? totalDistance / frameCount : 0);
        movementPatterns.put("aggressivePositions", aggressivePositions);
        movementPatterns.put("aggressivenessScore", calculateAggressivenessScore(aggressivePositions, frameCount));
        movementPatterns.put("engagementStyle", determineEngagementStyle(zoneTimeSpent, frameCount));
        
        // 결과 조합
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("positionHistory", positionHistory);
        analysis.put("zoneTimeSpent", zoneTimeSpent);
        analysis.put("movementPatterns", movementPatterns);
        analysis.put("riskAnalysis", riskAnalysis);
        
        return analysis;
    }
    
    @Override
    public String determineMapZone(int x, int y, int teamId) {
        // 칼바람 나락은 수평 맵이므로 X축 기준으로 구역 판정
        boolean isBlueTeam = teamId == 100;  // 100=블루팀, 200=레드팀
        
        if (isBlueTeam) {
            // 블루팀 기준 (왼쪽에서 시작)
            if (x < 4000) {
                return "friendlyZone";      // 아군 구역 (아군 타워 근처)
            } else if (x < 6000) {
                return "neutralZone";       // 중앙 구역
            } else if (x < 8000) {
                return "enemyZone";         // 적군 구역
            } else {
                return "dangerZone";        // 위험 구역 (적 타워/넥서스 근처)
            }
        } else {
            // 레드팀 기준 (오른쪽에서 시작)
            if (x > 8000) {
                return "friendlyZone";      // 아군 구역
            } else if (x > 6000) {
                return "neutralZone";       // 중앙 구역  
            } else if (x > 4000) {
                return "enemyZone";         // 적군 구역
            } else {
                return "dangerZone";        // 위험 구역
            }
        }
    }
    
    @Override
    public String evaluateRiskLevel(int x, int y, int teamId) {
        String zone = determineMapZone(x, y, teamId);
        
        switch (zone) {
            case "dangerZone":
                return "HIGH";      // 적 타워 근처는 고위험
            case "enemyZone":
                return "MEDIUM";    // 적군 구역은 중위험
            case "neutralZone":
                return "MEDIUM";    // 중앙은 교전 구역이므로 중위험
            case "friendlyZone":
                return "LOW";       // 아군 구역만 상대적으로 안전
            default:
                return "MEDIUM";
        }
    }
    
    @Override
    public Set<String> getImportantEventTypes() {
        return IMPORTANT_EVENTS;
    }
    
    /**
     * ARAM 위험도 분석 계산
     */
    private Map<String, Object> calculateRiskAnalysis(List<Map<String, Object>> positionHistory) {
        Map<String, Object> riskAnalysis = new HashMap<>();
        
        int highRisk = 0, mediumRisk = 0, lowRisk = 0;
        
        for (Map<String, Object> position : positionHistory) {
            String riskLevel = (String) position.get("riskLevel");
            switch (riskLevel) {
                case "HIGH": highRisk++; break;
                case "MEDIUM": mediumRisk++; break;
                case "LOW": lowRisk++; break;
            }
        }
        
        int total = positionHistory.size();
        if (total > 0) {
            riskAnalysis.put("highRiskPercentage", Math.round((highRisk * 100.0) / total));
            riskAnalysis.put("mediumRiskPercentage", Math.round((mediumRisk * 100.0) / total));
            riskAnalysis.put("safePercentage", Math.round((lowRisk * 100.0) / total));
        }
        
        return riskAnalysis;
    }
    
    /**
     * 공격성 점수 계산 (ARAM 특화)
     */
    private int calculateAggressivenessScore(int aggressivePositions, int frameCount) {
        if (frameCount == 0) return 0;
        
        double aggressivenessRatio = (double) aggressivePositions / frameCount;
        
        // 공격성 점수 (0-100)
        // ARAM에서는 적절한 공격성이 중요
        return Math.min(100, (int) (aggressivenessRatio * 100));
    }
    
    /**
     * 교전 스타일 판정 (ARAM 특화)
     */
    private String determineEngagementStyle(Map<String, Integer> zoneTimeSpent, int frameCount) {
        if (frameCount == 0) return "UNKNOWN";
        
        double friendlyRatio = (double) zoneTimeSpent.get("friendlyZone") / frameCount;
        double aggressiveRatio = (double) (zoneTimeSpent.get("enemyZone") + zoneTimeSpent.get("dangerZone")) / frameCount;
        
        if (aggressiveRatio > 0.4) {
            return "AGGRESSIVE";    // 공격적 스타일
        } else if (friendlyRatio > 0.6) {
            return "DEFENSIVE";     // 수비적 스타일  
        } else {
            return "BALANCED";      // 균형 스타일
        }
    }
}