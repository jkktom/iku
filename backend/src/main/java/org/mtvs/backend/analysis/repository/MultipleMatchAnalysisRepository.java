package org.mtvs.backend.analysis.repository;

import org.mtvs.backend.analysis.entity.MultipleMatchAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MultipleMatchAnalysisRepository extends JpaRepository<MultipleMatchAnalysis, Long> {
    
    /**
     * PUUID로 다중 매치 분석 조회
     */
    List<MultipleMatchAnalysis> findByPuuidOrderByCreatedAtDesc(String puuid);
    
    /**
     * 분석 상태별 조회 (페이징)
     */
    Page<MultipleMatchAnalysis> findByAnalysisStatusOrderByCreatedAtDesc(
            MultipleMatchAnalysis.AnalysisStatus status, Pageable pageable);
    
    /**
     * 분석 상태별 조회 (리스트)
     */
    List<MultipleMatchAnalysis> findByAnalysisStatusOrderByCreatedAtDesc(
            MultipleMatchAnalysis.AnalysisStatus status);
    
    /**
     * 완료된 분석 개수 조회
     */
    @Query("SELECT COUNT(m) FROM MultipleMatchAnalysis m WHERE m.analysisStatus = :status")
    long countByAnalysisStatus(@Param("status") MultipleMatchAnalysis.AnalysisStatus status);
    
    /**
     * 특정 사용자의 완료된 분석 개수 조회
     */
    @Query("SELECT COUNT(m) FROM MultipleMatchAnalysis m WHERE m.puuid = :puuid AND m.analysisStatus = :status")
    long countByPuuidAndAnalysisStatus(@Param("puuid") String puuid, 
                                      @Param("status") MultipleMatchAnalysis.AnalysisStatus status);
    
    /**
     * 처리 대기 중인 분석 조회
     */
    List<MultipleMatchAnalysis> findByAnalysisStatusInOrderByCreatedAtAsc(
            List<MultipleMatchAnalysis.AnalysisStatus> statuses);
    
    /**
     * 실패한 분석 조회
     */
    List<MultipleMatchAnalysis> findByAnalysisStatusOrderByUpdatedAtDesc(
            MultipleMatchAnalysis.AnalysisStatus status);
    
    /**
     * 특정 사용자의 최근 분석 조회 (제한된 개수)
     */
    @Query("SELECT m FROM MultipleMatchAnalysis m WHERE m.puuid = :puuid ORDER BY m.createdAt DESC")
    List<MultipleMatchAnalysis> findTopByPuuidOrderByCreatedAtDesc(@Param("puuid") String puuid, 
                                                                  Pageable pageable);
    
    /**
     * 특정 매치 개수로 분석 조회
     */
    List<MultipleMatchAnalysis> findByPuuidAndMatchCountOrderByCreatedAtDesc(String puuid, Integer matchCount);
    
    /**
     * 특정 기간 내 분석 조회
     */
    @Query("SELECT m FROM MultipleMatchAnalysis m WHERE m.puuid = :puuid AND m.createdAt >= :startDate ORDER BY m.createdAt DESC")
    List<MultipleMatchAnalysis> findByPuuidAndCreatedAtAfterOrderByCreatedAtDesc(
            @Param("puuid") String puuid, 
            @Param("startDate") java.time.LocalDateTime startDate);
    
    /**
     * 특정 매치 ID가 포함된 분석 조회 (JSONB 배열 검색)
     */
    @Query(value = "SELECT * FROM multiple_match_analysis WHERE analyzed_match_ids @> to_jsonb(?1::text) ORDER BY created_at DESC", 
           nativeQuery = true)
    List<MultipleMatchAnalysis> findByAnalyzedMatchIdsContaining(@Param("matchId") String matchId);
    
    /**
     * 최근 분석 결과 조회 (사용자별 최신 N개)
     */
    @Query("SELECT m FROM MultipleMatchAnalysis m WHERE m.puuid = :puuid AND m.analysisStatus = :status ORDER BY m.createdAt DESC")
    List<MultipleMatchAnalysis> findRecentAnalysisByPuuidAndStatus(@Param("puuid") String puuid, 
                                                                  @Param("status") MultipleMatchAnalysis.AnalysisStatus status, 
                                                                  Pageable pageable);
}