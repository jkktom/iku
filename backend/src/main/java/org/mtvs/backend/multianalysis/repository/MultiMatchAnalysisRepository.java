package org.mtvs.backend.multianalysis.repository;

import org.mtvs.backend.multianalysis.entity.MultiMatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MultiMatchAnalysisRepository extends JpaRepository<MultiMatchAnalysis, Long> {
    
    List<MultiMatchAnalysis> findByPuuidOrderByCreatedAtDesc(String puuid);
    
    Optional<MultiMatchAnalysis> findByPuuidAndGameNameAndTagLineAndMatchCount(
            String puuid, String gameName, String tagLine, int matchCount);
    
    @Query("SELECT m FROM MultiMatchAnalysis m WHERE m.puuid = :puuid AND m.status = 'COMPLETED' ORDER BY m.completedAt DESC")
    List<MultiMatchAnalysis> findCompletedanalysisByPuuid(@Param("puuid") String puuid);
    
    @Query("SELECT COUNT(m) FROM MultiMatchAnalysis m WHERE m.puuid = :puuid")
    long countByPuuid(@Param("puuid") String puuid);
}