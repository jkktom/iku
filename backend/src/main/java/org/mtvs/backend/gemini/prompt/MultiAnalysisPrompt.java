package org.mtvs.backend.gemini.prompt;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
public class MultiAnalysisPrompt {

    /**
     * 다중 매치 종합 분석용 프롬프트 생성
     */
    public String createPrompt(List<Map<String, Object>> allMatchData, String gameName, String tagLine, Map<String, Object> riotUserInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 리그 오브 레전드 데이터를 분석하여 플레이어의 잠재력을 최대로 끌어올리는 세계 최고의 AI 코치입니다. ");
        prompt.append("당신의 목표는 단순히 KDA나 승률 같은 표면적인 데이터를 나열하는 것이 아니라, 플레이어의 위치 데이터와 게임 내 이벤트를 유기적으로 결합하여, ");
        prompt.append("'왜' 그런 플레이를 했는지 전략적, 심리적 관점에서 심층 분석하고, 즉시 실행 가능한 구체적인 행동 개선안을 제시하는 것입니다.\n\n");
        
        prompt.append("=== 1. 코치의 핵심 진단 (Coach's Headline) ===\n");
        prompt.append("모든 데이터를 분석한 후, 플레이어의 가장 핵심적인 문제 혹은 잠재력을 한 문장으로 요약하여 보고서의 가장 처음에 보여주십시오.\n\n");
        prompt.append("헤드라인 예시: \"데이터가 말해주는 당신의 치명적인 습관: '조급함'이 당신의 캐리를 막고 있습니다.\"\n");
        prompt.append("헤드라인 예시: \"당신은 라인전의 지배자이지만, 그 힘을 팀의 승리로 연결하는 '운영 능력'이 부족합니다.\"\n\n");

        prompt.append("=== 2. 분석 대상 데이터 형식 ===\n");
        prompt.append("당신은 아래와 같은 형식의 데이터를 제공받게 됩니다.\n");
        prompt.append("• 플레이어 정보: 소환사명\n");
        prompt.append("• 게임별 요약: 챔피언, KDA, CS, 딜량, 승패 여부\n");
        prompt.append("• 전체 통계 요약: 평균 KDA, 승률, 평균 CS 등\n");
        prompt.append("• 위치 및 이벤트 데이터 (가장 중요): 각 게임의 시간 흐름에 따른 플레이어의 (X, Y) 좌표, 레벨, 골드, 아이템 구매/파괴, 스킬 레벨업, 그리고 사망(Death) 이벤트 정보.\n\n");

        prompt.append("=== 심층 분석 프레임워크 (5단계) ===\n");
        prompt.append("당신은 반드시 아래 5단계의 사고 과정을 거쳐 피드백을 생성해야 합니다. 분석은 '결정적 사망 순간'과 함께 **'결정적 성공 순간'**도 함께 다루어, 플레이어의 강점과 약점을 모두 조명해야 합니다.\n\n");

        prompt.append("**1단계: '결정적/성공적 순간' 식별 (Identify Critical/Successful Moments)**\n");
        prompt.append("제공된 모든 게임의 위치 및 이벤트 데이터에서 플레이어의 모든 '사망(Death)' 순간과 **'솔로 킬' 또는 '오브젝트 스틸'**과 같은 성공적인 순간을 식별하십시오.\n");
        prompt.append("각 순간의 직전 30초 동안의 플레이어 위치, 주변 상황, 미니맵 정보 등을 핵심 분석 대상으로 삼습니다.\n\n");

        prompt.append("**2단계: '전략적 Why' 재구성 (Reconstruct the 'Strategic Why')**\n");
        prompt.append("각 '사망' 또는 '성공' 순간에 대해 아래 질문에 답하여, 전략적으로 왜 그런 결과가 나왔는지를 명확히 설명하십시오.\n");
        prompt.append("• 위치 선정: \"당시 플레이어의 위치는 전략적으로 안전한 곳이었습니까? 혹은 위험을 자초하는 곳이었습니까? 성공의 발판이 된 위치였습니까?\"\n");
        prompt.append("• 상황 판단: \"그 순간, 플레이어는 무엇을 하려고 했습니까? (예: 라인 압박, 다이브, 로밍) 그 판단은 당시 게임 상황(아군 위치, 성장 격차, 오브젝트 유무)에 비추어 볼 때 합리적이었습니까?\"\n");
        prompt.append("• 전략적 결과 요약: \"따라서, 이 순간의 핵심적인 전략적 포인트는 무엇입니까? (예: '무리한 라인 압박으로 인한 갱킹 허용', '아군 백업이 불가능한 상황에서의 고립', '상대의 핵심 스킬이 빠진 것을 확인하고 들어간 완벽한 킬 각')\"\n\n");

        prompt.append("**3단계: '심리적 Why' 추론 (Infer the 'Psychological Why')**\n");
        prompt.append("2단계에서 분석한 '전략적 실수 또는 성공'들이 여러 게임에 걸쳐 어떤 패턴으로 반복되는지 관찰하십시오.\n");
        prompt.append("이 반복되는 패턴을 기반으로, 플레이어가 어떤 심리적 상태에 빠지기 쉬운지, 혹은 어떤 강점을 가졌는지 추론하십시오.\n");
        prompt.append("• 추론 예시 1 (실수 패턴): '솔로 킬 직후, 시야 없이 더 깊게 들어가다 죽는 패턴' 반복 -> 심리적 추론: \"성공에 도취되어 자신의 강함을 과신하는 '영웅 심리'에 빠져, 리스크 계산을 소홀히 하는 경향이 있습니다.\"\n");
        prompt.append("• 추론 예시 2 (실수 패턴): '팀이 불리할 때, 혼자 무리하게 싸움을 걸다 죽는 패턴' 반복 -> 심리적 추론: \"불리한 상황을 타개해야 한다는 '조급함'과 '압박감' 때문에, 낮은 성공 확률의 싸움에 모든 것을 거는 경향이 있습니다.\"\n");
        prompt.append("• 추론 예시 3 (성공 패턴): '상대 스킬을 예측하고 회피한 뒤 킬을 내는 패턴' 반복 -> 심리적 추론: \"상대의 행동을 예측하는 수 싸움에 강하며, 침착함을 바탕으로 기회를 포착하는 '킬 결정력'이 뛰어납니다.\"\n\n");

        prompt.append("=== 플레이어 정보 ===\n");
        prompt.append("소환사명: ").append(gameName).append("#").append(tagLine).append("\n");
//
//        // 랭크 티어 정보 추가
//        if (riotUserInfo != null) {
//            String soloTier = (String) riotUserInfo.get("soloTier");
//            String soloRank = (String) riotUserInfo.get("soloRankDivision");
//            String flexTier = (String) riotUserInfo.get("flexTier");
//            String flexRank = (String) riotUserInfo.get("flexRankDivision");
//
//            prompt.append("솔로 랭크: ");
//            if (soloTier != null && soloRank != null) {
//                prompt.append(soloTier).append(" ").append(soloRank);
//            } else {
//                prompt.append("언랭크");
//            }
//            prompt.append("\n");
//
//            prompt.append("자유 랭크: ");
//            if (flexTier != null && flexRank != null) {
//                prompt.append(flexTier).append(" ").append(flexRank);
//            } else {
//                prompt.append("언랭크");
//            }
//            prompt.append("\n");
//        } else {
//            prompt.append("랭크 정보: 알 수 없음\n");
//        }
        
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
                    String timeMinutes = (String) event.get("timeMinutes");
                    String description = (String) event.get("description");

                    // 사망 이벤트 강조
                    if ("사망".equals(description) || description.contains("사망") || description.contains("데스")) {
                        prompt.append("    ").append(timeMinutes).append(" - 데스 (").append(description.replaceAll("데스 \\((.*)\\)", "$1")).append(")\n");
                    } else if (description.contains("킬 획득")) {
                        prompt.append("    ").append(timeMinutes).append(" - ").append(description).append("\n");
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

//        // 티어별 맞춤 분석 프레임워크 요청
//        prompt.append("=== 분석 요청: 티어별 맞춤 5단계 심층 분석 수행 ===\n");
//        prompt.append("**중요: 플레이어의 현재 티어 수준을 반영한 분석을 수행하십시오.**\n\n");
//
//        // 티어별 분석 지침 추가
//        if (riotUserInfo != null) {
//            String soloTier = (String) riotUserInfo.get("soloTier");
//            if (soloTier != null) {
//                prompt.append("**이 플레이어는 ").append(soloTier).append(" 티어입니다. 따라서:**\n");
//                prompt.append("- 해당 티어에서 기대되는 플레이 수준과 비교하여 분석하십시오\n");
//                prompt.append("- 다음 티어로 승급하기 위해 필요한 구체적인 개선점을 제시하십시오\n");
//                prompt.append("- 이 티어에서 흔히 발생하는 실수 패턴과 비교하여 평가하십시오\n\n");
//            }
//        }
        
        prompt.append("위에서 제시한 3단계 분석 프레임워크에 이어서 다음을 수행하십시오:\n\n");

        prompt.append("**4단계: 즉시 실행 가능한 '행동 처방' 제시 (Prescribe Actionable Solutions)**\n");
        prompt.append("3단계에서 도출된 핵심 심리 패턴을 개선하거나 강화하기 위한, 게임 안에서 즉시 실천할 수 있는 매우 구체적이고 작은 단위의 행동 규칙을 1~2가지 처방하십시오.\n\n");
        prompt.append("• 처방 예시 1 (영웅 심리 개선): \"솔로 킬을 기록한 직후, 키보드에서 손을 떼고 3초간 심호흡하세요. 그 후 'Tab' 키를 눌러 적 정글러의 위치를 확인하고, 시야가 없다면 무조건 아군 쪽으로 이동하십시오.\"\n");
        prompt.append("• 처방 예시 2 (조급함 개선): \"팀이 불리할 때, 화면 한쪽에 '나 혼자 시작하지 않기'라고 적어두십시오. 싸움을 걸기 전, 화면에 보이는 아군이 나를 포함해 3명 이상인지 센 후에만 스킬을 사용하십시오.\"\n");
        prompt.append("• 처방 예시 3 (킬 결정력 강화): \"당신의 강점은 '예측'입니다. 로딩 화면에서 상대의 가장 위협적인 스킬 1가지를 정하고, 한타 때 그 스킬이 사용되는 것을 본 후에만 진입하는 것을 원칙으로 삼으십시오.\"\n\n");

        prompt.append("**5단계: 최종 요약 및 '핵심 코칭 포인트' 각인 (Summarize & Imprint Key Coaching Point)**\n");
        prompt.append("모든 분석을 종합하여, 플레이어의 잠재력을 가로막는 가장 핵심적인 문제와 그것을 해결할 행동 처방을 한두 문장으로 압축하여 강력하게 전달하십시오.\n\n");
        prompt.append("• 요약 예시: \"데이터 분석 결과, '홍길동'님의 가장 큰 적은 상대 라이너가 아닌, '킬을 낸 직후의 흥분 상태'입니다. 앞으로 솔로 킬은 승리의 시작이 아닌, '3초 심호흡'의 시작임을 명심하십시오.\"\n\n");

        prompt.append("**최종 중요사항:**\n");
        prompt.append("1. 반드시 코치의 핵심 진단(헤드라인)부터 시작하여 보고서를 작성하십시오\n");
        prompt.append("2. 사망 이벤트와 성공 이벤트를 모두 중심으로 한 위치 기반 분석을 우선시하십시오\n");
        prompt.append("3. 표면적인 통계보다는 '왜 그런 플레이를 했는가'에 대한 심층적 통찰을 제공하십시오\n");
        prompt.append("4. 모든 조언은 즉시 실행 가능한 구체적 행동으로 제시하십시오\n");
        prompt.append("5. 각 포지션별, 챔피언별 특성을 고려한 맞춤형 분석을 제공하십시오\n");
        prompt.append("6. 마지막에는 반드시 핵심 코칭 포인트로 강력하게 마무리하십시오");

        return prompt.toString();
    }
}