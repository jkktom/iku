package org.mtvs.backend.analysis.repository;

import org.mtvs.backend.analysis.entity.AnalysisStatus;
import org.mtvs.backend.analysis.entity.SingleMatchAnalysis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SingleMatchAnalysisRepository extends BaseAnalysisRepository<SingleMatchAnalysis> {
    
    /**
     * PUUID와 매치 ID로 단일 매치 분석 조회 (SingleMatchAnalysis 특화 메서드)
     */
    Optional<SingleMatchAnalysis> findByPuuidAndMatchId(String puuid, String matchId);
    
    /**
     * 특정 매치 ID로 모든 분석 조회 (SingleMatchAnalysis 특화 메서드)
     */
    List<SingleMatchAnalysis> findByMatchIdOrderByCreatedAtDesc(String matchId);
    
    /**
     * 특정 사용자의 완료된 분석 개수 조회 (SingleMatchAnalysis 특화 메서드)
     */
    @Query("SELECT COUNT(s) FROM SingleMatchAnalysis s WHERE s.puuid = :puuid AND s.analysisStatus = :status")
    long countByPuuidAndAnalysisStatus(@Param("puuid") String puuid, 
                                      @Param("status") AnalysisStatus status);
    
    /**
     * 특정 사용자의 최근 분석 조회 (제한된 개수) - SingleMatchAnalysis 특화 메서드
     */
    @Query("SELECT s FROM SingleMatchAnalysis s WHERE s.puuid = :puuid ORDER BY s.createdAt DESC")
    List<SingleMatchAnalysis> findTopByPuuidOrderByCreatedAtDesc(@Param("puuid") String puuid, 
                                                                Pageable pageable);
    
    /**
     * 중복 분석 확인 - SingleMatchAnalysis 특화 메서드
     */
    boolean existsByPuuidAndMatchId(String puuid, String matchId);
    
    /**
     * 특정 기간 내 분석 조회 - SingleMatchAnalysis 특화 메서드
     */
    @Query("SELECT s FROM SingleMatchAnalysis s WHERE s.puuid = :puuid AND s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<SingleMatchAnalysis> findByPuuidAndCreatedAtAfterOrderByCreatedAtDesc(
            @Param("puuid") String puuid, 
            @Param("startDate") java.time.LocalDateTime startDate);
}