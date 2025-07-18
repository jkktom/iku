package org.mtvs.backend.analysis.repository;

import org.mtvs.backend.analysis.entity.AnalysisStatus;
import org.mtvs.backend.analysis.entity.SingleMatchAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SingleMatchAnalysisRepository extends JpaRepository<SingleMatchAnalysis, Long> {
    
    /**
     * PUUID로 단일 매치 분석 조회
     */
    List<SingleMatchAnalysis> findByPuuidOrderByCreatedAtDesc(String puuid);
    
    /**
     * PUUID와 매치 ID로 단일 매치 분석 조회
     */
    Optional<SingleMatchAnalysis> findByPuuidAndMatchId(String puuid, String matchId);
    
    /**
     * 분석 상태별 조회 (페이징)
     */
    Page<SingleMatchAnalysis> findByAnalysisStatusOrderByCreatedAtDesc(
            AnalysisStatus status, Pageable pageable);
    
    /**
     * 분석 상태별 조회 (리스트)
     */
    List<SingleMatchAnalysis> findByAnalysisStatusOrderByCreatedAtDesc(
            AnalysisStatus status);
    
    /**
     * 특정 매치 ID로 모든 분석 조회
     */
    List<SingleMatchAnalysis> findByMatchIdOrderByCreatedAtDesc(String matchId);
    
    /**
     * 완료된 분석 개수 조회
     */
    @Query("SELECT COUNT(s) FROM SingleMatchAnalysis s WHERE s.analysisStatus = :status")
    long countByAnalysisStatus(@Param("status") AnalysisStatus status);
    
    /**
     * 특정 사용자의 완료된 분석 개수 조회
     */
    @Query("SELECT COUNT(s) FROM SingleMatchAnalysis s WHERE s.puuid = :puuid AND s.analysisStatus = :status")
    long countByPuuidAndAnalysisStatus(@Param("puuid") String puuid, 
                                      @Param("status") AnalysisStatus status);
    
    /**
     * 처리 대기 중인 분석 조회
     */
    List<SingleMatchAnalysis> findByAnalysisStatusInOrderByCreatedAtAsc(
            List<AnalysisStatus> statuses);
    
    /**
     * 실패한 분석 조회
     */
    List<SingleMatchAnalysis> findByAnalysisStatusOrderByUpdatedAtDesc(
            AnalysisStatus status);
    
    /**
     * 특정 사용자의 최근 분석 조회 (제한된 개수)
     */
    @Query("SELECT s FROM SingleMatchAnalysis s WHERE s.puuid = :puuid ORDER BY s.createdAt DESC")
    List<SingleMatchAnalysis> findTopByPuuidOrderByCreatedAtDesc(@Param("puuid") String puuid, 
                                                                Pageable pageable);
    
    /**
     * 중복 분석 확인
     */
    boolean existsByPuuidAndMatchId(String puuid, String matchId);
    
    /**
     * 특정 기간 내 분석 조회
     */
    @Query("SELECT s FROM SingleMatchAnalysis s WHERE s.puuid = :puuid AND s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<SingleMatchAnalysis> findByPuuidAndCreatedAtAfterOrderByCreatedAtDesc(
            @Param("puuid") String puuid, 
            @Param("startDate") java.time.LocalDateTime startDate);
}