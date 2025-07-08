package org.mtvs.backend.multianalysis.service;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MultiMatchPromptBuilder {
    
    public String buildAnalysisPrompt(List<Map<String, Object>> allMatchData, String gameName, String tagLine) {
        StringBuilder prompt = new StringBuilder();
        
        addIntroduction(prompt);
        addDataFormatExplanation(prompt);
        addAnalysisFramework(prompt);
        addPlayerInfo(prompt, gameName, tagLine, allMatchData.size());
        addGameSummaries(prompt, allMatchData);
        addOverallStatistics(prompt, allMatchData);
        addPositionAndEventData(prompt, allMatchData);
        addAnalysisRequest(prompt);
        
        return prompt.toString();
    }
    
    private void addIntroduction(StringBuilder prompt) {
        prompt.append("당신은 리그 오브 레전드 데이터를 분석하여 플레이어의 잠재력을 최대로 끌어올리는 세계 최고의 AI 코치입니다. ");
        prompt.append("당신의 목표는 단순히 KDA나 승률 같은 표면적인 데이터를 나열하는 것이 아니라, 플레이어의 위치 데이터와 게임 내 이벤트를 유기적으로 결합하여, ");
        prompt.append("'왜' 그런 플레이를 했는지 전략적, 심리적 관점에서 심층 분석하고, 즉시 실행 가능한 구체적인 행동 개선안을 제시하는 것입니다.\n\n");
    }
    
    private void addDataFormatExplanation(StringBuilder prompt) {
        prompt.append("=== 2. 분석 대상 데이터 형식 ===\n");
        prompt.append("당신은 아래와 같은 형식의 데이터를 제공받게 됩니다.\n");
        prompt.append("• 플레이어 정보: 소환사명\n");
        prompt.append("• 게임별 요약: 챔피언, KDA, CS, 딜량, 승패 여부\n");
        prompt.append("• 전체 통계 요약: 평균 KDA, 승률, 평균 CS 등\n");
        prompt.append("• 위치 및 이벤트 데이터 (가장 중요): 각 게임의 시간 흐름에 따른 플레이어의 (X, Y) 좌표, 레벨, 골드, 아이템 구매/파괴, 스킬 레벨업, 그리고 사망(Death) 이벤트 정보.\n\n");
    }
    
    private void addAnalysisFramework(StringBuilder prompt) {
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
    }
    
    private void addPlayerInfo(StringBuilder prompt, String gameName, String tagLine, int matchCount) {
        prompt.append("=== 플레이어 정보 ===\n");
        prompt.append("소환사명: ").append(gameName).append("#").append(tagLine).append("\n");
        prompt.append("분석 대상: 최근 ").append(matchCount).append("게임의 종합 성과\n\n");
    }
    
    private void addGameSummaries(StringBuilder prompt, List<Map<String, Object>> allMatchData) {
        prompt.append("=== 게임별 요약 ===\n");
        
        for (Map<String, Object> matchData : allMatchData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> playerInfo = (Map<String, Object>) matchData.get("playerInfo");
            @SuppressWarnings("unchecked")
            Map<String, Object> finalStats = (Map<String, Object>) matchData.get("finalStats");
            
            int matchIndex = (Integer) matchData.get("matchIndex");
            String champion = (String) playerInfo.get("championName");
            String result = (String) playerInfo.get("result");
            
            int kills = (Integer) finalStats.get("kills");
            int deaths = (Integer) finalStats.get("deaths");
            int assists = (Integer) finalStats.get("assists");
            int cs = (Integer) finalStats.get("totalCS");
            int damage = (Integer) finalStats.get("damageDealt");
            
            prompt.append("게임 ").append(matchIndex).append(": ")
                  .append(champion).append(" - ")
                  .append(result).append(" (")
                  .append(kills).append("/").append(deaths).append("/").append(assists).append(", ")
                  .append("CS: ").append(cs).append(", ")
                  .append("딜량: ").append(String.format("%,d", damage)).append(")\n");
        }
    }
    
    private void addOverallStatistics(StringBuilder prompt, List<Map<String, Object>> allMatchData) {
        int totalKills = 0, totalDeaths = 0, totalAssists = 0;
        int totalCS = 0, totalGold = 0, totalDamage = 0;
        int wins = 0;
        List<String> champions = new ArrayList<>();
        
        for (Map<String, Object> matchData : allMatchData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> playerInfo = (Map<String, Object>) matchData.get("playerInfo");
            @SuppressWarnings("unchecked")
            Map<String, Object> finalStats = (Map<String, Object>) matchData.get("finalStats");
            
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
        }
        
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
    }
    
    private void addPositionAndEventData(StringBuilder prompt, List<Map<String, Object>> allMatchData) {
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
            
            if (events != null && !events.isEmpty()) {
                prompt.append("  주요 이벤트 (사망 포함):\n");
                for (Map<String, Object> event : events) {
                    String timeMinutes = (String) event.get("timeMinutes");
                    String description = (String) event.get("description");
                    
                    if ("사망".equals(description) || description.contains("사망")) {
                        prompt.append("  ⚠️  ").append(timeMinutes).append(" - **사망 이벤트** (중요 분석 대상)\n");
                    } else {
                        prompt.append("    ").append(timeMinutes).append(" - ").append(description).append("\n");
                    }
                }
            }
            
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
    }
    
    private void addAnalysisRequest(StringBuilder prompt) {
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
    }
}