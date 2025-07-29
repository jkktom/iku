package org.mtvs.backend.gemini.prompt;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class SingleAnalysisPrompt {

    /**
     * 개인 분석용 프롬프트 생성
     */
    public String createPrompt(Map<String, Object> playerData, String matchId) {
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
        @SuppressWarnings("unchecked")
        Map<String, Object> riotUserInfo = (Map<String, Object>) playerData.get("riotUserInfo");

        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 리그 오브 레전드 전문 개인 코치입니다.\n\n");

        // 플레이어 기본 정보
        prompt.append("=== 플레이어 정보 ===\n");
        prompt.append("소환사명: ").append(playerInfo.get("summonerName")).append("\n");
        prompt.append("챔피언: ").append(playerInfo.get("championName")).append("\n");
        prompt.append("포지션: ").append(playerInfo.get("teamPosition")).append("\n");
        
        // 랭크 티어 정보 추가
        if (riotUserInfo != null) {
            String soloTier = (String) riotUserInfo.get("soloTier");
            String soloRank = (String) riotUserInfo.get("soloRankDivision");
            String flexTier = (String) riotUserInfo.get("flexTier");
            String flexRank = (String) riotUserInfo.get("flexRankDivision");
            
            prompt.append("솔로 랭크: ");
            if (soloTier != null && soloRank != null) {
                prompt.append(soloTier).append(" ").append(soloRank);
            } else {
                prompt.append("언랭크");
            }
            prompt.append("\n");
            
            prompt.append("자유 랭크: ");
            if (flexTier != null && flexRank != null) {
                prompt.append(flexTier).append(" ").append(flexRank);
            } else {
                prompt.append("언랭크");
            }
            prompt.append("\n");
        } else {
            prompt.append("랭크 정보: 알 수 없음\n");
        }
        
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

        // 분석 요청 (티어와 포지션, 챔피언 맞춤형)
        prompt.append("이 데이터를 바탕으로 다음 항목들을 상세히 분석해주세요:\n\n");
        
        prompt.append("**중요: 플레이어의 현재 포지션, 선택한 챔피언의 특성을 반드시 고려하여 분석해주세요.**\n");


        
//        // 티어별 맞춤 분석 요청
//        if (riotUserInfo != null) {
//            String soloTier = (String) riotUserInfo.get("soloTier");
//            String position = (String) playerInfo.get("teamPosition");
//            String champion = (String) playerInfo.get("championName");
//
//            if (soloTier != null) {
//                prompt.append("- 이 플레이어는 **").append(soloTier).append(" 티어**이므로, 해당 티어에 맞는 수준의 기대치와 개선점을 제시해주세요.\n");
//                prompt.append("- **").append(position).append(" 포지션**의 ").append(champion).append(" 챔피언으로서 이 티어에서 필요한 핵심 스킬과 역할을 중심으로 분석해주세요.\n\n");
//            }
//        }

        prompt.append("1. **라인전 및 초반 운영** (0-15분)\n");
        prompt.append("   - 해당 티어 기준 파밍 효율성 및 CS 관리 평가\n");
        prompt.append("   - 이 포지션과 챔피언에 맞는 초반 플레이 패턴 분석\n");
        prompt.append("   - 킬/데스 패턴과 티어별 기대 수준 비교\n");
        prompt.append("   - 초반 골드 운영의 효율성\n\n");

        prompt.append("2. **중반 게임 운영** (15-25분)\n");
        prompt.append("   - 해당 포지션의 중반 역할 수행도 평가\n");
        prompt.append("   - 오브젝트 기여도 (티어 기준 비교)\n");
        prompt.append("   - 팀파이트에서 이 챔피언의 역할 수행도\n");
        prompt.append("   - 로밍 및 맵 압박 (포지션별 기대치 고려)\n\n");

        prompt.append("3. **후반 캐리력** (25분+)\n");
        prompt.append("   - 해당 티어에서 기대되는 딜량 기여도 평가\n");
        prompt.append("   - 이 챔피언과 포지션에 맞는 포지셔닝 및 생존력\n");
        prompt.append("   - 게임 결정력 (티어별 판단력 기준)\n\n");

        prompt.append("4. **포지셔닝 및 맵 운영**\n");
        prompt.append("   - 해당 포지션에서 요구되는 맵 구역별 활동 패턴 평가\n");
        prompt.append("   - 티어 수준에 맞는 로밍 타이밍과 효율성\n");
        prompt.append("   - 위험 지역 관리 능력 (티어별 기대 수준)\n");
        prompt.append("   - 이동성과 맵 압박 기여도\n\n");
////
////        prompt.append("5. **티어별 맞춤 개선 가이드**\n");
////        prompt.append("   - 현재 티어에서 다음 티어로 승급하기 위한 핵심 개선점 3가지\n");
////        prompt.append("   - 이 포지션과 챔피언으로 해당 티어에서 승률을 높이는 방법\n");
//        prompt.append("   - 다음 게임에서 집중할 포인트 (티어 수준 고려)\n");
//        prompt.append("   - 포지셔닝 및 맵 운영 개선 방안 (포지션별 특화)\n\n");

        prompt.append("각 분석은 구체적인 시간대와 상황을 언급하며, 플레이어의 티어와 포지션, 챔피언에 맞는 실행 가능한 조언으로 제공해주세요.\n");
        prompt.append("특히 플레이와 비교하여 어떤 부분이 우수하고 어떤 부분이 부족한지 명확히 지적해주세요.\n");
        prompt.append("전체적인 평가와 함께 해당 티어에서 승급하기 위해 가장 개선이 필요한 부분을 우선순위로 제시해주세요.");

        return prompt.toString();
    }
}