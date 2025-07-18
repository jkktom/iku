package org.mtvs.backend.gemini.prompt;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DuoAnalysisPrompt {
    /**
     * 듀오 매치 비교 분석용 프롬프트 생성
     */
    public String createPrompt(Map<String, Object> player1Data,
                               Map<String, Object> player2Data,
                               String player1Name, String player1Tag,
                               String player2Name, String player2Tag,
                               String matchId) {
        StringBuilder prompt = new StringBuilder();

        // 시스템 역할 정의
        prompt.append("당신은 리그 오브 레전드 전문 듀오 분석 코치입니다. ");
        prompt.append("두 플레이어가 같은 게임에서 보여준 성과를 심층적으로 비교 분석하고, ");
        prompt.append("듀오 시너지 향상을 위한 구체적이고 실행 가능한 조언을 제공하는 것이 목표입니다.\n\n");

        // 분석 접근 방식
        prompt.append("=== 분석 접근 방식 ===\n");
        prompt.append("당신은 단순한 수치 비교를 넘어서, 다음과 같은 관점에서 분석해야 합니다:\n");
        prompt.append("• 개별 성과 vs 팀 기여도의 균형\n");
        prompt.append("• 두 플레이어 간의 상호 보완성과 시너지\n");
        prompt.append("• 협력 패턴과 소통의 효율성\n");
        prompt.append("• 게임 흐름에 따른 역할 분담과 적응력\n\n");

        // 기본 정보
        prompt.append("=== 매치 정보 ===\n");
        prompt.append("매치 ID: ").append(matchId).append("\n");
        prompt.append("플레이어 1: ").append(player1Name).append("#").append(player1Tag).append("\n");
        prompt.append("플레이어 2: ").append(player2Name).append("#").append(player2Tag).append("\n\n");

        // 게임 결과
        prompt.append(buildGameResult(player1Data));

        // 플레이어 성과 비교
        prompt.append(buildPlayerComparison(player1Data, player2Data, player1Name, player2Name));

        // 타임라인 분석
        prompt.append(buildTimelineAnalysis(player1Data, player2Data, player1Name, player2Name));

        // 포지셔닝 분석
        prompt.append(buildPositionAnalysis(player1Data, player2Data, player1Name, player2Name));

        // 분석 프레임워크 요청
        prompt.append(buildAnalysisFramework());

        return prompt.toString();
    }

    /**
     * 게임 결과 섹션 구성
     */
    private String buildGameResult(Map<String, Object> player1Data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> playerInfo = (Map<String, Object>) player1Data.get("playerInfo");
        @SuppressWarnings("unchecked")
        Map<String, Object> gameInfo = (Map<String, Object>) player1Data.get("gameInfo");

        StringBuilder result = new StringBuilder();
        result.append("=== 게임 결과 ===\n");
        result.append("게임 모드: ").append(gameInfo.get("queueType")).append("\n");
        result.append("게임 시간: ").append(String.format("%.1f분", (Long)gameInfo.get("duration") / 60.0)).append("\n");
        result.append("게임 결과: ").append(playerInfo.get("result")).append("\n\n");

        return result.toString();
    }

    /**
     * 플레이어 성과 비교 섹션 구성
     */
    private String buildPlayerComparison(Map<String, Object> player1Data, Map<String, Object> player2Data,
                                         String player1Name, String player2Name) {
        StringBuilder comparison = new StringBuilder();
        comparison.append("=== 플레이어 성과 비교 ===\n");

        // 플레이어 1 정보
        comparison.append(buildIndividualStats(player1Data, player1Name, "플레이어 1"));
        comparison.append("\n");

        // 플레이어 2 정보
        comparison.append(buildIndividualStats(player2Data, player2Name, "플레이어 2"));
        comparison.append("\n");

        // 핵심 지표 비교
        comparison.append(buildKeyMetricsComparison(player1Data, player2Data));
        comparison.append("\n");

        return comparison.toString();
    }

    /**
     * 개별 플레이어 통계 구성
     */
    private String buildIndividualStats(Map<String, Object> playerData, String playerName, String label) {
        @SuppressWarnings("unchecked")
        Map<String, Object> playerInfo = (Map<String, Object>) playerData.get("playerInfo");
        @SuppressWarnings("unchecked")
        Map<String, Object> finalStats = (Map<String, Object>) playerData.get("finalStats");

        StringBuilder stats = new StringBuilder();
        stats.append(label).append(" (").append(playerName).append("):\n");
        stats.append("  - 챔피언: ").append(playerInfo.get("championName")).append("\n");
        stats.append("  - KDA: ").append(finalStats.get("kills")).append("/")
                .append(finalStats.get("deaths")).append("/").append(finalStats.get("assists")).append("\n");
        stats.append("  - CS: ").append(finalStats.get("totalCS")).append("\n");
        stats.append("  - 골드: ").append(String.format("%,d", (Integer)finalStats.get("goldEarned"))).append("\n");
        stats.append("  - 딜량: ").append(String.format("%,d", (Integer)finalStats.get("damageDealt"))).append("\n");
        stats.append("  - 받은 피해: ").append(String.format("%,d", (Integer)finalStats.get("damageTaken"))).append("\n");
        stats.append("  - 시야점수: ").append(finalStats.get("visionScore"));

        return stats.toString();
    }

    /**
     * 핵심 지표 비교 구성
     */
    private String buildKeyMetricsComparison(Map<String, Object> player1Data, Map<String, Object> player2Data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> p1Stats = (Map<String, Object>) player1Data.get("finalStats");
        @SuppressWarnings("unchecked")
        Map<String, Object> p2Stats = (Map<String, Object>) player2Data.get("finalStats");

        StringBuilder comparison = new StringBuilder();
        comparison.append("=== 핵심 지표 비교 ===\n");

        // KDA 비교
        double p1KDA = calculateKDA(p1Stats);
        double p2KDA = calculateKDA(p2Stats);
        comparison.append("KDA 비교: ").append(String.format("%.2f", p1KDA))
                .append(" vs ").append(String.format("%.2f", p2KDA))
                .append(" (차이: ").append(String.format("%.2f", Math.abs(p1KDA - p2KDA))).append(")\n");

        // 골드 효율성 비교
        int p1Gold = (Integer) p1Stats.get("goldEarned");
        int p2Gold = (Integer) p2Stats.get("goldEarned");
        comparison.append("골드 획득: ").append(String.format("%,d", p1Gold))
                .append(" vs ").append(String.format("%,d", p2Gold))
                .append(" (차이: ").append(String.format("%,d", Math.abs(p1Gold - p2Gold))).append(")\n");

        // 딜량 비교
        int p1Damage = (Integer) p1Stats.get("damageDealt");
        int p2Damage = (Integer) p2Stats.get("damageDealt");
        comparison.append("딜량: ").append(String.format("%,d", p1Damage))
                .append(" vs ").append(String.format("%,d", p2Damage))
                .append(" (차이: ").append(String.format("%,d", Math.abs(p1Damage - p2Damage))).append(")\n");

        return comparison.toString();
    }

    /**
     * 타임라인 분석 섹션 구성
     */
    private String buildTimelineAnalysis(Map<String, Object> player1Data, Map<String, Object> player2Data,
                                         String player1Name, String player2Name) {
        StringBuilder timeline = new StringBuilder();
        timeline.append("=== 타임라인 분석 (핵심 이벤트) ===\n");

        // 플레이어 1 이벤트
        timeline.append(buildPlayerEvents(player1Data, player1Name, "플레이어 1"));

        // 플레이어 2 이벤트
        timeline.append(buildPlayerEvents(player2Data, player2Name, "플레이어 2"));

        return timeline.toString();
    }

    /**
     * 개별 플레이어 이벤트 구성
     */
    private String buildPlayerEvents(Map<String, Object> playerData, String playerName, String label) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) playerData.get("timelineEvents");

        StringBuilder eventText = new StringBuilder();
        eventText.append(label).append(" (").append(playerName).append(") 주요 이벤트:\n");

        if (events != null && !events.isEmpty()) {
            for (Map<String, Object> event : events) {
                String description = (String) event.get("description");
                // 사망 이벤트 강조
                if (description.contains("사망")) {
                    eventText.append("  ⚠️  ").append(description).append(" (중요 분석 대상)\n");
                } else {
                    eventText.append("    ").append(description).append("\n");
                }
            }
        } else {
            eventText.append("  (이벤트 데이터 없음)\n");
        }
        eventText.append("\n");

        return eventText.toString();
    }

    /**
     * 포지셔닝 분석 섹션 구성
     */
    private String buildPositionAnalysis(Map<String, Object> player1Data, Map<String, Object> player2Data,
                                         String player1Name, String player2Name) {
        StringBuilder position = new StringBuilder();
        position.append("=== 포지셔닝 및 맵 활동 분석 ===\n");

        // 플레이어 1 포지셔닝
        position.append(buildPlayerPositioning(player1Data, player1Name, "플레이어 1"));

        // 플레이어 2 포지셔닝
        position.append(buildPlayerPositioning(player2Data, player2Name, "플레이어 2"));

        return position.toString();
    }

    /**
     * 개별 플레이어 포지셔닝 구성
     */
    private String buildPlayerPositioning(Map<String, Object> playerData, String playerName, String label) {
        @SuppressWarnings("unchecked")
        Map<String, Object> positionAnalysis = (Map<String, Object>) playerData.get("positionAnalysis");

        StringBuilder positioning = new StringBuilder();
        positioning.append(label).append(" (").append(playerName).append(") 포지셔닝:\n");

        if (positionAnalysis != null) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> zoneTimeSpent = (Map<String, Integer>) positionAnalysis.get("zoneTimeSpent");
            @SuppressWarnings("unchecked")
            Map<String, Object> riskAnalysis = (Map<String, Object>) positionAnalysis.get("riskAnalysis");
            @SuppressWarnings("unchecked")
            Map<String, Object> movementPatterns = (Map<String, Object>) positionAnalysis.get("movementPatterns");

            if (zoneTimeSpent != null) {
                positioning.append("  맵 활동 시간: ");
                positioning.append("적정글 ").append(zoneTimeSpent.get("enemyJungle")).append("분, ");
                positioning.append("자정글 ").append(zoneTimeSpent.get("ownJungle")).append("분, ");
                positioning.append("리버 ").append(zoneTimeSpent.get("river")).append("분\n");
            }

            if (riskAnalysis != null) {
                positioning.append("  위험도 분석: ");
                positioning.append("고위험 ").append(riskAnalysis.get("highRiskPercentage")).append("%, ");
                positioning.append("안전지역 ").append(riskAnalysis.get("safePercentage")).append("%\n");
            }

            if (movementPatterns != null) {
                positioning.append("  이동 패턴: ");
                positioning.append("로밍 ").append(movementPatterns.get("roamingCount")).append("회, ");
                positioning.append("기동성 점수 ").append(movementPatterns.get("mobilityScore")).append("\n");
            }
        } else {
            positioning.append("  (포지셔닝 데이터 없음)\n");
        }
        positioning.append("\n");

        return positioning.toString();
    }

    /**
     * 분석 프레임워크 요청 섹션 구성
     */
    private String buildAnalysisFramework() {
        StringBuilder framework = new StringBuilder();
        framework.append("=== 듀오 분석 프레임워크 (4단계) ===\n");
        framework.append("위의 데이터를 바탕으로 다음 4단계로 분석해주세요:\n\n");

        framework.append("**1단계: 개별 성과 평가 (Individual Performance Assessment)**\n");
        framework.append("각 플레이어의 개별 성과를 객관적으로 평가하십시오.\n");
        framework.append("• 챔피언 역할 대비 기대 성과 달성도\n");
        framework.append("• 게임 단계별 기여도 (초반/중반/후반)\n");
        framework.append("• 주요 강점과 개선이 필요한 약점\n");
        framework.append("• 사망 패턴 분석 및 포지셔닝 평가\n\n");

        framework.append("**2단계: 듀오 시너지 분석 (Duo Synergy Analysis)**\n");
        framework.append("두 플레이어 간의 협력과 상호 보완성을 분석하십시오.\n");
        framework.append("• 챔피언 조합의 시너지 효과\n");
        framework.append("• 게임 내 역할 분담의 효율성\n");
        framework.append("• 협력이 잘 이루어진 순간들\n");
        framework.append("• 소통 부족이나 불협화음이 있었던 부분\n\n");

        framework.append("**3단계: 개선 기회 식별 (Improvement Opportunities)**\n");
        framework.append("구체적이고 실행 가능한 개선 방안을 제시하십시오.\n");
        framework.append("• 각 플레이어가 개별적으로 개선할 점\n");
        framework.append("• 듀오 협력 향상을 위한 구체적 방법\n");
        framework.append("• 다음 게임에서 집중해야 할 핵심 포인트\n");
        framework.append("• 장기적인 듀오 발전 방향\n\n");

        framework.append("**4단계: 종합 평가 및 권장사항 (Overall Assessment & Recommendations)**\n");
        framework.append("이번 듀오 플레이에 대한 종합 평가를 제공하십시오.\n");
        framework.append("• 전체적인 듀오 플레이 등급 (S/A/B/C/D)\n");
        framework.append("• 이 듀오 조합의 잠재력과 한계\n");
        framework.append("• 향후 추천 챔피언 조합\n");
        framework.append("• 단계별 성장 로드맵 (단기/중기/장기)\n\n");

        framework.append("**중요 지침:**\n");
        framework.append("• 수치 데이터와 구체적인 게임 상황을 근거로 분석\n");
        framework.append("• 비판적이되 건설적인 피드백 제공\n");
        framework.append("• 즉시 적용 가능한 실용적 조언 우선\n");
        framework.append("• 두 플레이어 모두에게 균형 잡힌 관점 제시\n\n");

        return framework.toString();
    }

    /**
     * KDA 계산 헬퍼 메서드
     */
    private double calculateKDA(Map<String, Object> stats) {
        int kills = (Integer) stats.get("kills");
        int deaths = (Integer) stats.get("deaths");
        int assists = (Integer) stats.get("assists");

        if (deaths == 0) {
            return kills + assists;
        }
        return (double) (kills + assists) / deaths;
    }
}
