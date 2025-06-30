package org.mtvs.backend.riot.Repository;

import org.mtvs.backend.riot.entity.MatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchAnalysisRepository extends JpaRepository<MatchAnalysis, Long> {
    
    // puuid로 분석 이력 조회
    List<MatchAnalysis> findByPuuidOrderByCreatedAtDesc(String puuid);
    
    // matchId로 분석 결과 조회
    List<MatchAnalysis> findByMatchIdOrderByCreatedAtDesc(String matchId);
    
    // puuid와 matchId로 특정 분석 조회
    Optional<MatchAnalysis> findByPuuidAndMatchId(String puuid, String matchId);
    
    // puuid로 matchId가 null인 레코드 조회 (미완료 레코드)
    List<MatchAnalysis> findByPuuidAndMatchIdIsNullOrderByCreatedAtDesc(String puuid);
    
    // puuid로 matchId가 null이 아닌 레코드 조회 (완료된 레코드)
    List<MatchAnalysis> findByPuuidAndMatchIdIsNotNullOrderByCreatedAtDesc(String puuid);
    
    // 분석 상태별 조회
    List<MatchAnalysis> findByAnalysisStatusOrderByCreatedAtDesc(MatchAnalysis.AnalysisStatus status);
    
    // 특정 유저의 특정 상태 분석들 조회
    List<MatchAnalysis> findByPuuidAndAnalysisStatusOrderByCreatedAtDesc(
        String puuid, MatchAnalysis.AnalysisStatus status);
    
    // 특정 챔피언의 분석 결과 조회
    List<MatchAnalysis> findByTargetChampionOrderByCreatedAtDesc(String championName);
    
    // 분석 중이거나 요청된 것들 조회 (재처리용)
    @Query("SELECT ma FROM MatchAnalysis ma WHERE ma.analysisStatus IN ('REQUESTED', 'PROCESSING')")
    List<MatchAnalysis> findPendingAnalysis();
    
    // 실패한 분석들 조회
    @Query("SELECT ma FROM MatchAnalysis ma WHERE ma.analysisStatus = 'FAILED' AND ma.errorMessage IS NOT NULL")
    List<MatchAnalysis> findFailedAnalysis();
    
    // 특정 기간의 분석 조회
    @Query("SELECT ma FROM MatchAnalysis ma WHERE ma.createdAt >= :startDate ORDER BY ma.createdAt DESC")
    List<MatchAnalysis> findAnalysisAfterDate(@Param("startDate") java.time.LocalDateTime startDate);
    
    // 중복 분석 방지를 위한 존재 확인
    boolean existsByPuuidAndMatchId(String puuid, String matchId);
}
