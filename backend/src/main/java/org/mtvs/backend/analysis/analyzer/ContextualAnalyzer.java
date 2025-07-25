package org.mtvs.backend.analysis.analyzer;

import org.mtvs.backend.riot.dto.InfoDto;
import org.mtvs.backend.riot.dto.ParticipantDto;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 컨텍스트 기반 분석기
 * 게임 시간, 포지션, 팀 비교, 게임 상황을 고려한 의미있는 지표 계산
 */
@Component
public class ContextualAnalyzer {
    
    // 실제 통계 기반 티어별 + 포지션별 평균 기준값들
    private static final Map<String, Map<String, Map<String, Double>>> TIER_POSITION_AVERAGES = Map.of(
        "IRON", Map.of(
            "TOP", Map.of("damagePerMinute", 370.0, "wardsPerMinute", 0.47, "controlWardsPerGame", 2.0, "visionScorePerMinute", 0.47),
            "JUNGLE", Map.of("damagePerMinute", 310.0, "wardsPerMinute", 0.67, "controlWardsPerGame", 2.5, "visionScorePerMinute", 0.67),
            "MIDDLE", Map.of("damagePerMinute", 400.0, "wardsPerMinute", 0.50, "controlWardsPerGame", 1.8, "visionScorePerMinute", 0.50),
            "BOTTOM", Map.of("damagePerMinute", 420.0, "wardsPerMinute", 0.37, "controlWardsPerGame", 1.5, "visionScorePerMinute", 0.37),
            "UTILITY", Map.of("damagePerMinute", 180.0, "wardsPerMinute", 2.5, "controlWardsPerGame", 5.5, "visionScorePerMinute", 1.0)
        ),
        "BRONZE", Map.of(
            "TOP", Map.of("damagePerMinute", 420.0, "wardsPerMinute", 0.57, "controlWardsPerGame", 2.2, "visionScorePerMinute", 0.57),
            "JUNGLE", Map.of("damagePerMinute", 350.0, "wardsPerMinute", 0.83, "controlWardsPerGame", 3.0, "visionScorePerMinute", 0.83),
            "MIDDLE", Map.of("damagePerMinute", 450.0, "wardsPerMinute", 0.60, "controlWardsPerGame", 2.0, "visionScorePerMinute", 0.60),
            "BOTTOM", Map.of("damagePerMinute", 470.0, "wardsPerMinute", 0.47, "controlWardsPerGame", 1.8, "visionScorePerMinute", 0.47),
            "UTILITY", Map.of("damagePerMinute", 210.0, "wardsPerMinute", 2.7, "controlWardsPerGame", 6.0, "visionScorePerMinute", 1.33)
        ),
        "SILVER", Map.of(
            "TOP", Map.of("damagePerMinute", 470.0, "wardsPerMinute", 0.67, "controlWardsPerGame", 2.5, "visionScorePerMinute", 0.67),
            "JUNGLE", Map.of("damagePerMinute", 390.0, "wardsPerMinute", 1.0, "controlWardsPerGame", 3.5, "visionScorePerMinute", 1.0),
            "MIDDLE", Map.of("damagePerMinute", 500.0, "wardsPerMinute", 0.73, "controlWardsPerGame", 2.2, "visionScorePerMinute", 0.73),
            "BOTTOM", Map.of("damagePerMinute", 520.0, "wardsPerMinute", 0.57, "controlWardsPerGame", 2.0, "visionScorePerMinute", 0.57),
            "UTILITY", Map.of("damagePerMinute", 240.0, "wardsPerMinute", 2.9, "controlWardsPerGame", 6.5, "visionScorePerMinute", 1.67)
        ),
        "GOLD", Map.of(
            "TOP", Map.of("damagePerMinute", 520.0, "wardsPerMinute", 0.77, "controlWardsPerGame", 2.8, "visionScorePerMinute", 0.77),
            "JUNGLE", Map.of("damagePerMinute", 430.0, "wardsPerMinute", 1.17, "controlWardsPerGame", 4.0, "visionScorePerMinute", 1.17),
            "MIDDLE", Map.of("damagePerMinute", 550.0, "wardsPerMinute", 0.87, "controlWardsPerGame", 2.5, "visionScorePerMinute", 0.87),
            "BOTTOM", Map.of("damagePerMinute", 580.0, "wardsPerMinute", 0.67, "controlWardsPerGame", 2.2, "visionScorePerMinute", 0.67),
            "UTILITY", Map.of("damagePerMinute", 270.0, "wardsPerMinute", 3.1, "controlWardsPerGame", 7.0, "visionScorePerMinute", 2.0)
        ),
        "PLATINUM", Map.of(
            "TOP", Map.of("damagePerMinute", 580.0, "wardsPerMinute", 0.87, "controlWardsPerGame", 3.2, "visionScorePerMinute", 0.87),
            "JUNGLE", Map.of("damagePerMinute", 480.0, "wardsPerMinute", 1.33, "controlWardsPerGame", 4.5, "visionScorePerMinute", 1.33),
            "MIDDLE", Map.of("damagePerMinute", 620.0, "wardsPerMinute", 1.0, "controlWardsPerGame", 2.8, "visionScorePerMinute", 1.0),
            "BOTTOM", Map.of("damagePerMinute", 650.0, "wardsPerMinute", 0.77, "controlWardsPerGame", 2.5, "visionScorePerMinute", 0.77),
            "UTILITY", Map.of("damagePerMinute", 300.0, "wardsPerMinute", 3.3, "controlWardsPerGame", 7.5, "visionScorePerMinute", 2.33)
        ),
        "DIAMOND", Map.of(
            "TOP", Map.of("damagePerMinute", 650.0, "wardsPerMinute", 1.0, "controlWardsPerGame", 3.8, "visionScorePerMinute", 1.0),
            "JUNGLE", Map.of("damagePerMinute", 550.0, "wardsPerMinute", 1.5, "controlWardsPerGame", 5.0, "visionScorePerMinute", 1.5),
            "MIDDLE", Map.of("damagePerMinute", 700.0, "wardsPerMinute", 1.17, "controlWardsPerGame", 3.2, "visionScorePerMinute", 1.17),
            "BOTTOM", Map.of("damagePerMinute", 750.0, "wardsPerMinute", 0.87, "controlWardsPerGame", 3.0, "visionScorePerMinute", 0.87),
            "UTILITY", Map.of("damagePerMinute", 350.0, "wardsPerMinute", 3.5, "controlWardsPerGame", 8.0, "visionScorePerMinute", 2.67)
        ),
        "MASTER", Map.of(
            "TOP", Map.of("damagePerMinute", 680.0, "wardsPerMinute", 1.17, "controlWardsPerGame", 4.0, "visionScorePerMinute", 1.17),
            "JUNGLE", Map.of("damagePerMinute", 580.0, "wardsPerMinute", 1.67, "controlWardsPerGame", 5.5, "visionScorePerMinute", 1.67),
            "MIDDLE", Map.of("damagePerMinute", 720.0, "wardsPerMinute", 1.33, "controlWardsPerGame", 3.5, "visionScorePerMinute", 1.33),
            "BOTTOM", Map.of("damagePerMinute", 780.0, "wardsPerMinute", 1.0, "controlWardsPerGame", 3.2, "visionScorePerMinute", 1.0),
            "UTILITY", Map.of("damagePerMinute", 380.0, "wardsPerMinute", 3.7, "controlWardsPerGame", 8.5, "visionScorePerMinute", 2.8)
        ),
        "GRANDMASTER", Map.of(
            "TOP", Map.of("damagePerMinute", 700.0, "wardsPerMinute", 1.33, "controlWardsPerGame", 4.2, "visionScorePerMinute", 1.33),
            "JUNGLE", Map.of("damagePerMinute", 600.0, "wardsPerMinute", 1.83, "controlWardsPerGame", 6.0, "visionScorePerMinute", 1.83),
            "MIDDLE", Map.of("damagePerMinute", 750.0, "wardsPerMinute", 1.5, "controlWardsPerGame", 3.8, "visionScorePerMinute", 1.5),
            "BOTTOM", Map.of("damagePerMinute", 820.0, "wardsPerMinute", 1.17, "controlWardsPerGame", 3.5, "visionScorePerMinute", 1.17),
            "UTILITY", Map.of("damagePerMinute", 400.0, "wardsPerMinute", 3.9, "controlWardsPerGame", 9.0, "visionScorePerMinute", 3.0)
        ),
        "CHALLENGER", Map.of(
            "TOP", Map.of("damagePerMinute", 720.0, "wardsPerMinute", 1.5, "controlWardsPerGame", 4.5, "visionScorePerMinute", 1.5),
            "JUNGLE", Map.of("damagePerMinute", 620.0, "wardsPerMinute", 2.0, "controlWardsPerGame", 6.5, "visionScorePerMinute", 2.0),
            "MIDDLE", Map.of("damagePerMinute", 780.0, "wardsPerMinute", 1.67, "controlWardsPerGame", 4.0, "visionScorePerMinute", 1.67),
            "BOTTOM", Map.of("damagePerMinute", 850.0, "wardsPerMinute", 1.33, "controlWardsPerGame", 3.8, "visionScorePerMinute", 1.33),
            "UTILITY", Map.of("damagePerMinute", 420.0, "wardsPerMinute", 4.1, "controlWardsPerGame", 9.5, "visionScorePerMinute", 3.2)
        )
    );
    
    // 기본 기준값 (티어 정보가 없을 때 사용, GOLD 기준)
    private static final Map<String, Map<String, Double>> DEFAULT_POSITION_AVERAGES = 
        TIER_POSITION_AVERAGES.get("GOLD");
    
    /**
     * 컨텍스트 기반 종합 분석 수행 (티어 정보 포함)
     */
    public Map<String, Object> performContextualAnalysis(InfoDto gameInfo, ParticipantDto targetPlayer, String position, String tier) {
        Map<String, Object> analysis = new HashMap<>();
        
        double gameDurationMinutes = gameInfo.getGameDuration() / 60.0;
        boolean gameWon = targetPlayer.isWin();
        
        // 1. 피해량 효율성 분석 (티어 기준 적용)
        Map<String, Object> damageAnalysis = analyzeDamageEfficiency(targetPlayer, position, gameDurationMinutes, tier);
        
        // 2. 시야 기여도 분석 (티어 기준 적용)
        Map<String, Object> visionAnalysis = analyzeVisionContribution(targetPlayer, position, gameDurationMinutes, tier);
        
        // 3. 커뮤니케이션 분석
        Map<String, Object> communicationAnalysis = analyzeCommunicationPatterns(targetPlayer, gameDurationMinutes);
        
        // 4. 경제 효율성 분석 (티어 기준 적용)
        Map<String, Object> economicAnalysis = analyzeEconomicEfficiency(targetPlayer, position, gameDurationMinutes, tier);
        
        // 5. 게임 상황별 퍼포먼스
        Map<String, Object> situationalAnalysis = analyzeSituationalPerformance(targetPlayer, gameWon, gameDurationMinutes);
        
        analysis.put("gameContext", Map.of(
            "gameDurationMinutes", Math.round(gameDurationMinutes * 10.0) / 10.0,
            "gamePhase", determineGamePhase(gameDurationMinutes),
            "gameResult", gameWon ? "승리" : "패배",
            "position", position,
            "tier", tier != null ? tier : "UNRANKED"
        ));
        
        analysis.put("damageAnalysis", damageAnalysis);
        analysis.put("visionAnalysis", visionAnalysis);
        analysis.put("communicationAnalysis", communicationAnalysis);
        analysis.put("economicAnalysis", economicAnalysis);
        analysis.put("situationalAnalysis", situationalAnalysis);
        
        // 종합 평가 점수
        analysis.put("overallRating", calculateOverallRating(damageAnalysis, visionAnalysis, communicationAnalysis, economicAnalysis));
        
        return analysis;
    }
    
    /**
     * 하위 호환성을 위한 오버로드 메서드 (티어 정보 없음)
     */
    public Map<String, Object> performContextualAnalysis(InfoDto gameInfo, ParticipantDto targetPlayer, String position) {
        return performContextualAnalysis(gameInfo, targetPlayer, position, null);
    }
    
    /**
     * 티어별 기준값 가져오기
     */
    private Map<String, Double> getPositionAverages(String position, String tier) {
        // tier가 null이거나 빈 문자열인 경우 기본값 사용
        if (tier == null || tier.trim().isEmpty()) {
            return DEFAULT_POSITION_AVERAGES.getOrDefault(position, DEFAULT_POSITION_AVERAGES.get("MIDDLE"));
        }
        
        Map<String, Map<String, Double>> tierAverages = TIER_POSITION_AVERAGES.get(tier.toUpperCase());
        if (tierAverages != null && tierAverages.containsKey(position)) {
            return tierAverages.get(position);
        }
        // 티어 정보가 없거나 잘못된 경우 기본값 사용
        return DEFAULT_POSITION_AVERAGES.getOrDefault(position, DEFAULT_POSITION_AVERAGES.get("MIDDLE"));
    }

    /**
     * 피해량 효율성 분석 (티어 정보 포함)
     */
    private Map<String, Object> analyzeDamageEfficiency(ParticipantDto player, String position, double gameDurationMinutes, String tier) {
        Map<String, Object> analysis = new HashMap<>();
        
        double totalDamage = player.getTotalDamageDealtToChampions();
        double magicDamage = player.getMagicDamageDealtToChampions();
        double physicalDamage = player.getPhysicalDamageDealtToChampions();
        double trueDamage = player.getTrueDamageDealtToChampions();
        
        // 분당 피해량
        double damagePerMinute = totalDamage / gameDurationMinutes;
        
        // 티어별 포지션 대비 효율성
        Map<String, Double> positionAverages = getPositionAverages(position, tier);
        double expectedDPM = positionAverages.get("damagePerMinute");
        double damageEfficiency = (damagePerMinute / expectedDPM) * 100;
        
        // 피해 구성 분석
        Map<String, Double> damageComposition = Map.of(
            "physicalPercent", Math.round((physicalDamage / totalDamage) * 1000.0) / 10.0,
            "magicPercent", Math.round((magicDamage / totalDamage) * 1000.0) / 10.0,
            "truePercent", Math.round((trueDamage / totalDamage) * 1000.0) / 10.0
        );
        
        analysis.put("totalDamage", (int) totalDamage);
        analysis.put("damagePerMinute", Math.round(damagePerMinute));
        analysis.put("damageEfficiency", Math.round(damageEfficiency * 10.0) / 10.0);
        analysis.put("positionRating", getDamageRating(damageEfficiency));
        analysis.put("damageComposition", damageComposition);
        
        return analysis;
    }
    
    /**
     * 시야 기여도 분석 (티어 정보 포함)
     */
    private Map<String, Object> analyzeVisionContribution(ParticipantDto player, String position, double gameDurationMinutes, String tier) {
        Map<String, Object> analysis = new HashMap<>();
        
        int wardsPlaced = player.getWardsPlaced();
        int wardsKilled = player.getWardsKilled();
        int controlWards = player.getControlWardsPlaced();
        int visionScore = player.getVisionScore();
        
        // 분당 시야 지표들
        double wardsPerMinute = wardsPlaced / gameDurationMinutes;
        double visionScorePerMinute = visionScore / gameDurationMinutes;
        
        // 티어별 포지션 대비 평가
        Map<String, Double> positionAverages = getPositionAverages(position, tier);
        
        double wardEfficiency = (wardsPerMinute / positionAverages.get("wardsPerMinute")) * 100;
        double controlWardEfficiency = (controlWards / positionAverages.get("controlWardsPerGame")) * 100;
        double visionEfficiency = (visionScorePerMinute / positionAverages.get("visionScorePerMinute")) * 100;
        
        // 시야 제거 효율성
        double wardKillRatio = wardsPlaced > 0 ? (double) wardsKilled / wardsPlaced : 0;
        
        analysis.put("wardsPlaced", wardsPlaced);
        analysis.put("wardsKilled", wardsKilled);
        analysis.put("controlWardsPlaced", controlWards);
        analysis.put("visionScore", visionScore);
        analysis.put("wardsPerMinute", Math.round(wardsPerMinute * 100.0) / 100.0);
        analysis.put("visionScorePerMinute", Math.round(visionScorePerMinute * 100.0) / 100.0);
        analysis.put("wardEfficiency", Math.round(wardEfficiency * 10.0) / 10.0);
        analysis.put("controlWardEfficiency", Math.round(controlWardEfficiency * 10.0) / 10.0);
        analysis.put("visionEfficiency", Math.round(visionEfficiency * 10.0) / 10.0);
        analysis.put("wardKillRatio", Math.round(wardKillRatio * 100.0) / 100.0);
        analysis.put("positionRating", getVisionRating(visionEfficiency));
        
        return analysis;
    }
    
    /**
     * 커뮤니케이션 패턴 분석
     */
    private Map<String, Object> analyzeCommunicationPatterns(ParticipantDto player, double gameDurationMinutes) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 총 핑 수 계산
        int totalPings = player.getAllInPings() + player.getAssistMePings() + player.getBaitPings() +
                        player.getCommandPings() + player.getDangerPings() + player.getEnemyMissingPings() +
                        player.getEnemyVisionPings() + player.getGetBackPings() + player.getHoldPings() +
                        player.getNeedVisionPings() + player.getOnMyWayPings() + player.getPushPings() +
                        player.getVisionClearedPings();
        
        double pingsPerMinute = totalPings / gameDurationMinutes;
        
        // 핑 타입별 분석
        Map<String, Object> pingBreakdown = Map.of(
            "strategicPings", player.getAssistMePings() + player.getOnMyWayPings() + player.getPushPings(),
            "warningPings", player.getDangerPings() + player.getEnemyMissingPings() + player.getGetBackPings(),
            "visionPings", player.getEnemyVisionPings() + player.getNeedVisionPings() + player.getVisionClearedPings(),
            "aggressivePings", player.getAllInPings() + player.getBaitPings()
        );
        
        // 커뮤니케이션 스타일 분석
        String communicationStyle = determineCommunicationStyle(player, totalPings);
        
        analysis.put("totalPings", totalPings);
        analysis.put("pingsPerMinute", Math.round(pingsPerMinute * 100.0) / 100.0);
        analysis.put("pingBreakdown", pingBreakdown);
        analysis.put("communicationStyle", communicationStyle);
        analysis.put("communicationRating", getCommunicationRating(pingsPerMinute));
        
        return analysis;
    }
    
    /**
     * 경제 효율성 분석 (티어 정보 포함)
     */
    private Map<String, Object> analyzeEconomicEfficiency(ParticipantDto player, String position, double gameDurationMinutes, String tier) {
        Map<String, Object> analysis = new HashMap<>();
        
        int goldEarned = player.getGoldEarned();
        int totalCs = player.getTotalMinionsKilled() + player.getNeutralMinionsKilled();
        
        double goldPerMinute = goldEarned / gameDurationMinutes;
        double csPerMinute = totalCs / gameDurationMinutes;
        
        // 골드 효율성 (피해량 대비)
        double damagePerGold = (double) player.getTotalDamageDealtToChampions() / goldEarned * 1000;
        
        // CS 효율성
        String csRating = getCsRating(csPerMinute, position);
        
        analysis.put("goldEarned", goldEarned);
        analysis.put("totalCs", totalCs);
        analysis.put("goldPerMinute", Math.round(goldPerMinute));
        analysis.put("csPerMinute", Math.round(csPerMinute * 10.0) / 10.0);
        analysis.put("damagePerGold", Math.round(damagePerGold * 100.0) / 100.0);
        analysis.put("csRating", csRating);
        analysis.put("economicEfficiency", getEconomicRating(goldPerMinute, position));
        
        return analysis;
    }
    
    /**
     * 게임 상황별 퍼포먼스 분석
     */
    private Map<String, Object> analyzeSituationalPerformance(ParticipantDto player, boolean gameWon, double gameDurationMinutes) {
        Map<String, Object> analysis = new HashMap<>();
        
        // KDA 계산
        double kda = player.getDeaths() > 0 ? 
            (double) (player.getKills() + player.getAssists()) / player.getDeaths() : 
            player.getKills() + player.getAssists();
        
        // 게임 길이별 퍼포먼스
        String gamePhase = determineGamePhase(gameDurationMinutes);
        String performanceContext = getPerformanceContext(kda, gameWon, gamePhase);
        
        // 생존성 분석
        double survivalRate = gameDurationMinutes > 0 ? 
            (gameDurationMinutes * 60 - (player.getDeaths() * 30)) / (gameDurationMinutes * 60) * 100 : 100;
        survivalRate = Math.max(0, Math.min(100, survivalRate));
        
        analysis.put("kda", Math.round(kda * 100.0) / 100.0);
        analysis.put("kills", player.getKills());
        analysis.put("deaths", player.getDeaths());
        analysis.put("assists", player.getAssists());
        analysis.put("gamePhase", gamePhase);
        analysis.put("performanceContext", performanceContext);
        analysis.put("survivalRate", Math.round(survivalRate * 10.0) / 10.0);
        analysis.put("gameImpact", getGameImpactRating(kda, gameWon));
        
        return analysis;
    }
    
    // 헬퍼 메서드들
    private String determineGamePhase(double minutes) {
        if (minutes < 15) return "초반전";
        else if (minutes < 30) return "중반전";
        else return "후반전";
    }
    
    private String getDamageRating(double efficiency) {
        if (efficiency >= 120) return "매우 우수";
        else if (efficiency >= 100) return "우수";
        else if (efficiency >= 80) return "보통";
        else if (efficiency >= 60) return "부족";
        else return "매우 부족";
    }
    
    private String getVisionRating(double efficiency) {
        if (efficiency >= 130) return "매우 우수";
        else if (efficiency >= 100) return "우수";
        else if (efficiency >= 80) return "보통";
        else if (efficiency >= 60) return "부족";
        else return "매우 부족";
    }
    
    private String getCommunicationRating(double pingsPerMinute) {
        if (pingsPerMinute >= 4) return "매우 활발";
        else if (pingsPerMinute >= 2.5) return "활발";
        else if (pingsPerMinute >= 1.5) return "보통";
        else if (pingsPerMinute >= 0.8) return "소극적";
        else return "매우 소극적";
    }
    
    private String determineCommunicationStyle(ParticipantDto player, int totalPings) {
        if (totalPings < 10) return "조용한 타입";
        
        int strategic = player.getAssistMePings() + player.getOnMyWayPings() + player.getPushPings();
        int warning = player.getDangerPings() + player.getEnemyMissingPings() + player.getGetBackPings();
        int vision = player.getEnemyVisionPings() + player.getNeedVisionPings() + player.getVisionClearedPings();
        
        if (strategic > warning && strategic > vision) return "전략적 소통형";
        else if (warning > strategic && warning > vision) return "경고/수비형";
        else if (vision > strategic && vision > warning) return "시야 중시형";
        else return "균형 소통형";
    }
    
    private String getCsRating(double csPerMinute, String position) {
        double threshold = position.equals("UTILITY") ? 1.0 : position.equals("JUNGLE") ? 4.0 : 6.0;
        
        if (csPerMinute >= threshold * 1.3) return "매우 우수";
        else if (csPerMinute >= threshold * 1.1) return "우수";
        else if (csPerMinute >= threshold * 0.9) return "보통";
        else if (csPerMinute >= threshold * 0.7) return "부족";
        else return "매우 부족";
    }
    
    private String getEconomicRating(double goldPerMinute, String position) {
        double threshold = position.equals("UTILITY") ? 300.0 : 400.0;
        
        if (goldPerMinute >= threshold * 1.3) return "매우 우수";
        else if (goldPerMinute >= threshold * 1.1) return "우수";
        else if (goldPerMinute >= threshold * 0.9) return "보통";
        else if (goldPerMinute >= threshold * 0.7) return "부족";
        else return "매우 부족";
    }
    
    private String getPerformanceContext(double kda, boolean gameWon, String gamePhase) {
        if (gameWon && kda >= 3.0) return gamePhase + " 캐리 퍼포먼스";
        else if (gameWon && kda >= 2.0) return gamePhase + " 안정적 승리 기여";
        else if (gameWon) return gamePhase + " 팀플레이 승리";
        else if (kda >= 2.5) return gamePhase + " 개인 선전 패배";
        else if (kda >= 1.5) return gamePhase + " 평범한 패배";
        else return gamePhase + " 어려운 경기";
    }
    
    private String getGameImpactRating(double kda, boolean gameWon) {
        if (gameWon && kda >= 4.0) return "게임 캐리";
        else if (gameWon && kda >= 2.5) return "승리 기여";
        else if (gameWon) return "팀승 참여";
        else if (kda >= 3.0) return "선전 패배";
        else if (kda >= 1.5) return "보통 패배";
        else return "부진한 경기";
    }
    
    private Map<String, Object> calculateOverallRating(Map<String, Object> damage, Map<String, Object> vision, 
                                                      Map<String, Object> communication, Map<String, Object> economic) {
        // 각 영역별 점수 계산 (0-100)
        double damageScore = convertRatingToScore((String) damage.get("positionRating"));
        double visionScore = convertRatingToScore((String) vision.get("positionRating"));
        double commScore = convertRatingToScore((String) communication.get("communicationRating"));
        double econScore = convertRatingToScore((String) economic.get("economicEfficiency"));
        
        // 가중 평균 (피해량 30%, 시야 25%, 경제 25%, 소통 20%)
        double overallScore = (damageScore * 0.3) + (visionScore * 0.25) + (econScore * 0.25) + (commScore * 0.2);
        
        String overallGrade = getGradeFromScore(overallScore);
        
        return Map.of(
            "overallScore", Math.round(overallScore * 10.0) / 10.0,
            "overallGrade", overallGrade,
            "breakdown", Map.of(
                "damage", damageScore,
                "vision", visionScore,
                "economy", econScore,
                "communication", commScore
            )
        );
    }
    
    private double convertRatingToScore(String rating) {
        switch (rating) {
            case "매우 우수": case "매우 활발": return 95.0;
            case "우수": case "활발": return 85.0;
            case "보통": return 70.0;
            case "부족": case "소극적": return 50.0;
            case "매우 부족": case "매우 소극적": return 30.0;
            default: return 50.0;
        }
    }
    
    private String getGradeFromScore(double score) {
        if (score >= 90) return "S";
        else if (score >= 80) return "A";
        else if (score >= 70) return "B";
        else if (score >= 60) return "C";
        else return "D";
    }
    
    // 하위 호환성을 위한 오버로드 메서드들
    private Map<String, Object> analyzeDamageEfficiency(ParticipantDto player, String position, double gameDurationMinutes) {
        return analyzeDamageEfficiency(player, position, gameDurationMinutes, null);
    }
    
    private Map<String, Object> analyzeVisionContribution(ParticipantDto player, String position, double gameDurationMinutes) {
        return analyzeVisionContribution(player, position, gameDurationMinutes, null);
    }
    
    private Map<String, Object> analyzeEconomicEfficiency(ParticipantDto player, String position, double gameDurationMinutes) {
        return analyzeEconomicEfficiency(player, position, gameDurationMinutes, null);
    }
}