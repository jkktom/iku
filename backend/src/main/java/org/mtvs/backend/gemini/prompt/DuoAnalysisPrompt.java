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
                               String matchId,
                               Map<String, Object> player1RiotInfo,
                               Map<String, Object> player2RiotInfo) {
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
        prompt.append("플레이어 1: ").append(player1Name).append("#").append(player1Tag);
        prompt.append("\n");
        prompt.append("플레이어 2: ").append(player2Name).append("#").append(player2Tag);
        prompt.append("\n\n");

        // 게임 결과
        prompt.append(buildGameResult(player1Data));

        // 플레이어 성과 비교
        prompt.append(buildPlayerComparison(player1Data, player2Data, player1Name, player2Name));

        // 타임라인 분석
        prompt.append(buildTimelineAnalysis(player1Data, player2Data, player1Name, player2Name));

        // 포지셔닝 분석
        prompt.append(buildPositionAnalysis(player1Data, player2Data, player1Name, player2Name));

        // 분석 프레임워크 요청
        prompt.append(buildAnalysisFramework(player1Name, player1Tag, player2Name, player2Tag));

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
        stats.append("  - 포지션: ").append(playerInfo.get("teamPosition")).append("\n");
        stats.append("  - KDA: ").append(finalStats.get("kills")).append("/")
                .append(finalStats.get("deaths")).append("/").append(finalStats.get("assists")).append("\n");
        stats.append("  - CS: ").append(finalStats.get("totalCS")).append("\n");
        stats.append("  - 골드: ").append(String.format("%,d", (Integer)finalStats.get("goldEarned"))).append("\n");
        stats.append("  - 딜량: ").append(String.format("%,d", (Integer)finalStats.get("damageDealt"))).append("\n");

        return stats.toString();
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
            positioning.append("  맵 활동 시간: ");
            positioning.append("적정글 ").append(zoneTimeSpent.get("enemyJungle")).append("분, ");
            positioning.append("자정글 ").append(zoneTimeSpent.get("ownJungle")).append("분, ");
            positioning.append("리버 ").append(zoneTimeSpent.get("river")).append("분\n");
        } else {
            positioning.append("  (포지셔닝 데이터 없음)\n");
        }
        positioning.append("\n");

        return positioning.toString();
    }

    /**
     * 분석 프레임워크 요청 섹션 구성
     */
    private String buildAnalysisFramework(String player1Name, String player1Tag, String player2Name, String player2Tag) {
        StringBuilder framework = new StringBuilder();
        framework.append("=== 듀오 분석 프레임워크 (4단계) ===\n");
        framework.append("위의 데이터를 바탕으로 다음 4단계로 분석해주세요:\n\n");
        
        framework.append("**1단계: 개별 성과 평가 (Individual Assessment)**\n");
        framework.append("각 플레이어의 개별 성과를 객관적으로 평가하십시오.\n");

        framework.append("**2단계: 듀오 시너지 분석 (Duo Synergy)**\n");
        framework.append("두 플레이어의 협력과 상호 보완성을 분석하십시오.\n");

        framework.append("**3단계: 맞춤 개선 방안 (Specific Improvements)**\n");
        framework.append("각 플레이어와 듀오를 위한 구체적인 개선 방안을 제시하십시오.\n");

        framework.append("**4단계: 종합 평가 및 MVP 선정**\n");
        framework.append("듀오 플레이를 종합적으로 평가하고 MVP를 선정해주세요.\n");

        framework.append("=== 최종 출력 형식 ===\n");
        framework.append("분석이 끝나면, 최종 결과를 다음 두 가지 형식으로 나누어 제공해주세요:\n");
        framework.append("1. **JSON 데이터**: 분석 내용을 아래의 정해진 JSON 구조에 맞춰 정리해주세요. 이 데이터는 시스템에서 직접 사용됩니다.\n");
        framework.append("2. **친근한 피드백 (텍스트)**: 위 JSON 데이터를 바탕으로, 플레이어에게 직접 코칭하듯이 친근하고 이해하기 쉬운 말투로 피드백을 작성해주세요. 이 피드백은 \'원본 AI 리포트 보기\' 기능에 사용됩니다. 칭찬과 격려를 섞어 동기를 부여하는 톤을 유지해주세요.\n\n");

        framework.append("**듀오 분석용 JSON 형식:**\n");
        framework.append("```json\n");
        framework.append("{\n");
        framework.append("  \"matchInfo\": {\n");
        framework.append("    \"gameResult\": \"승리 또는 패배\",\n");
        framework.append("    \"gameDuration\": \"게임 시간 (예: 30분 15초)\"\n");
        framework.append("  },\n");
        framework.append("  \"playerComparison\": {\n");
        framework.append("    \"player1\": {\n");
        framework.append("      \"name\": \"").append(player1Name).append("#").append(player1Tag).append("\",\n");
        framework.append("      \"champion\": \"챔피언명\",\n");
        framework.append("      \"kda\": \"K/D/A\",\n");
        framework.append("      \"damage\": 0,\n");
        framework.append("      \"gold\": 0\n");
        framework.append("    },\n");
        framework.append("    \"player2\": {\n");
        framework.append("      \"name\": \"").append(player2Name).append("#").append(player2Tag).append("\",\n");
        framework.append("      \"champion\": \"챔피언명\",\n");
        framework.append("      \"kda\": \"K/D/A\",\n");
        framework.append("      \"damage\": 0,\n");
        framework.append("      \"gold\": 0\n");
        framework.append("    }\n");
        framework.append("  },\n");
        framework.append("  \"synergyAnalysis\": {\n");
        framework.append("    \"strengths\": [\"시너지 강점 1\", \"시너지 강점 2\"],\n");
        framework.append("    \"weaknesses\": [\"시너지 약점 1\", \"시너지 약점 2\"]\n");
        framework.append("  },\n");
        framework.append("  \"improvementPoints\": {\n");
        framework.append("    \"forPlayer1\": [\"플레이어1 개선점 1\", \"플레이어1 개선점 2\"],\n");
        framework.append("    \"forPlayer2\": [\"플레이어2 개선점 1\", \"플레이어2 개선점 2\"],\n");
        framework.append("    \"forDuo\": [\"듀오 개선점 1\", \"듀오 개선점 2\"]\n");
        framework.append("  },\n");
        framework.append("  \"mvp\": {\n");
        framework.append("    \"playerName\": \"선정된 플레이어 이름\",\n");
        framework.append("    \"reason\": \"선정 이유\"\n");
        framework.append("  }\n");
        framework.append("}\n");
        framework.append("```\n");

        return framework.toString();
    }
}