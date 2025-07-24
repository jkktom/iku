package org.mtvs.backend.analysis.analyzer;

import org.mtvs.backend.riot.dto.FrameDto;
import org.mtvs.backend.riot.dto.MatchTimelineDto;
import org.mtvs.backend.riot.dto.ParticipantFrameDto;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 소환사의 협곡 (Map ID: 11) 전용 분석기
 * 5개 구역 분석: 탑라인, 정글, 미드라인, 봇라인, 강
 */
@Component
public class SummonersRiftAnalyzer implements MapAnalyzer {
    
    private static final int MAP_ID = 11;
    private static final String MAP_NAME = "소환사의 협곡";
    
    // 중요한 이벤트 타입들
    private static final Set<String> IMPORTANT_EVENTS = Set.of(
        "CHAMPION_KILL", 
        "ELITE_MONSTER_KILL", 
        "BUILDING_KILL"
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
        
        // 구역별 시간 초기화
        zoneTimeSpent.put("topLane", 0);
        zoneTimeSpent.put("jungle", 0);
        zoneTimeSpent.put("midLane", 0);
        zoneTimeSpent.put("botLane", 0);
        zoneTimeSpent.put("river", 0);
        zoneTimeSpent.put("ownJungle", 0);
        zoneTimeSpent.put("enemyJungle", 0);
        
        double totalDistance = 0.0;
        int frameCount = 0;
        int roamingCount = 0;
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
                    
                    // 구역 체류 시간 계산 (분 단위)
                    zoneTimeSpent.put(zone, zoneTimeSpent.get(zone) + 1);
                    
                    // 로밍 카운트 (구역 변경 시)
                    if (!zone.equals(previousZone) && !previousZone.isEmpty()) {
                        roamingCount++;
                    }
                    previousZone = zone;
                    
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
        
        // 이동 패턴 분석
        Map<String, Object> movementPatterns = new HashMap<>();
        movementPatterns.put("totalDistance", totalDistance);
        movementPatterns.put("averageSpeed", frameCount > 0 ? totalDistance / frameCount : 0);
        movementPatterns.put("roamingCount", roamingCount);
        movementPatterns.put("mobilityScore", calculateMobilityScore(totalDistance, roamingCount, frameCount));
        
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
        // 소환사의 협곡 좌표계 기반 구역 판정
        // League of Legends 맵 크기: 약 15000x15000, 중앙점: (7500, 7500)
        
        boolean isBlueTeam = teamId == 100;  // 100=블루팀, 200=레드팀
        
        // Debug logging for coordinate analysis (임시로 100% 로깅)
        System.out.println("🗺️ Zone Detection - Coord: (" + x + ", " + y + "), Team: " + teamId + " (" + (isBlueTeam ? "Blue" : "Red") + ")");
        
        // 1. 먼저 정글 구역 판정 (팀별 구분)
        boolean isInJungle = false;
        String jungleType = null;
        
        if (isBlueTeam) {
            // 블루팀 (팀 100) - 왼쪽 아래가 자신의 정글
            if (x < 6500 && y < 6500) {
                isInJungle = true;
                jungleType = "ownJungle";
            } else if (x > 8500 && y > 8500) {
                isInJungle = true;
                jungleType = "enemyJungle";
            }
        } else {
            // 레드팀 (팀 200) - 오른쪽 위가 자신의 정글
            if (x > 8500 && y > 8500) {
                isInJungle = true;
                jungleType = "ownJungle";
            } else if (x < 6500 && y < 6500) {
                isInJungle = true;
                jungleType = "enemyJungle";
            }
        }
        
        // 2. 강 (River) - 맵 중앙 지역, 대각선 형태로 확장
        if ((x >= 6000 && x <= 9000 && y >= 6000 && y <= 9000) && !isInJungle) {
            // 중앙 강 지역이지만 정글이 아닌 경우
            if (Math.abs(x - y) < 2000) { // 대각선 강 지역
                return "river";
            }
        }
        
        // 3. 라인 구역 판정 (정글이 아닌 경우)
        if (!isInJungle) {
            // 탑 라인 - 맵 상단 (y 값이 큰 영역)
            if (y > 9000) {
                return "topLane";
            }
            
            // 봇 라인 - 맵 하단 (y 값이 작은 영역)  
            if (y < 6000) {
                return "botLane";
            }
            
            // 미드 라인 - 맵 중앙 대각선 (정글과 강이 아닌 중간 지역)
            if (x >= 5500 && x <= 9500 && y >= 6000 && y <= 9000) {
                return "midLane";
            }
        }
        
        // 4. 정글 구역 반환
        if (isInJungle && jungleType != null) {
            return jungleType;
        }
        
        // 5. 기본값 - 미분류 지역은 가장 가까운 라인으로 분류
        if (y > 7500) {
            return "topLane";
        } else if (y < 7500) {
            return "botLane";
        } else {
            return "midLane";
        }
    }
    
    @Override
    public String evaluateRiskLevel(int x, int y, int teamId) {
        boolean isBlueTeam = teamId == 100;  // 100=블루팀, 200=레드팀
        
        // 적팀 정글은 고위험
        String zone = determineMapZone(x, y, teamId);
        if ("enemyJungle".equals(zone)) {
            return "HIGH";
        }
        
        // 강은 중위험
        if ("river".equals(zone)) {
            return "MEDIUM";  
        }
        
        // 자팀 정글과 라인은 상대적으로 안전
        if ("ownJungle".equals(zone) || zone.contains("Lane")) {
            return "LOW";
        }
        
        return "MEDIUM";
    }
    
    @Override
    public Set<String> getImportantEventTypes() {
        return IMPORTANT_EVENTS;
    }
    
    /**
     * 위험도 분석 계산
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
     * 이동성 점수 계산
     */
    private int calculateMobilityScore(double totalDistance, int roamingCount, int frameCount) {
        if (frameCount == 0) return 0;
        
        double avgDistance = totalDistance / frameCount;
        double roamingRatio = (double) roamingCount / frameCount;
        
        // 이동성 점수 (0-100)
        return Math.min(100, (int) ((avgDistance / 100) + (roamingRatio * 50)));
    }
}