package org.mtvs.backend.analysis.analyzer;

import org.mtvs.backend.riot.dto.MatchTimelineDto;

import java.util.Map;

/**
 * 맵별 분석 로직을 담당하는 인터페이스
 * 각 맵의 특성에 맞는 위치 분석 및 위험도 평가를 수행
 */
public interface MapAnalyzer {
    
    /**
     * 해당 분석기가 지원하는 맵 ID 반환
     * @return 맵 ID (11: 소환사의 협곡, 12: 칼바람 나락, 30: 아레나)
     */
    int getSupportedMapId();
    
    /**
     * 맵 이름 반환
     * @return 맵 이름 (한국어)
     */
    String getMapName();
    
    /**
     * 플레이어의 위치 분석 수행
     * @param matchTimeline 매치 타임라인 데이터
     * @param participantId 분석 대상 플레이어 ID
     * @param teamId 플레이어의 팀 ID (100=블루팀, 200=레드팀)
     * @return 위치 분석 결과 (구역별 체류시간, 위험도, 이동패턴 등)
     */
    Map<String, Object> analyzePlayerPosition(MatchTimelineDto matchTimeline, int participantId, int teamId);
    
    /**
     * 좌표를 기반으로 맵 구역 판정
     * @param x X 좌표
     * @param y Y 좌표
     * @param teamId 플레이어의 팀 ID (100=블루팀, 200=레드팀)
     * @return 구역 이름
     */
    String determineMapZone(int x, int y, int teamId);
    
    /**
     * 좌표의 위험도 평가
     * @param x X 좌표
     * @param y Y 좌표
     * @param teamId 플레이어의 팀 ID (100=블루팀, 200=레드팀)
     * @return 위험도 레벨 ("HIGH", "MEDIUM", "LOW")
     */
    String evaluateRiskLevel(int x, int y, int teamId);
    
    /**
     * 해당 맵에서 분석에 중요한 이벤트 타입들 반환
     * @return 중요 이벤트 타입 집합
     */
    java.util.Set<String> getImportantEventTypes();
}