package org.mtvs.backend.analysis.repository;

import org.mtvs.backend.analysis.entity.DuoMatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.mtvs.backend.analysis.entity.AnalysisStatus;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface DuoMatchAnalysisRepository extends JpaRepository<DuoMatchAnalysis,Long> {

    List<DuoMatchAnalysis> findByAnalysisStatusOrderByCreatedAtDesc(AnalysisStatus status);

    List<DuoMatchAnalysis> findByAnalysisStatusOrderByCreatedAtDesc(AnalysisStatus status, Pageable pageable);

    long countByAnalysisStatus(AnalysisStatus status);

    List<DuoMatchAnalysis> findByAnalysisStatusInOrderByCreatedAtAsc(List<AnalysisStatus> statuses);

    List<DuoMatchAnalysis> findByAnalysisStatusOrderByUpdatedAtDesc(AnalysisStatus status);

    List<DuoMatchAnalysis> findByMatchIdOrderByCreatedAtDesc(String matchId);

    Optional<DuoMatchAnalysis> findByMatchIdAndPlayer1PuuidAndPlayer2Puuid(String matchId, String player1Puuid, String player2Puuid);

    List<DuoMatchAnalysis> findByPlayer1PuuidOrPlayer2PuuidOrderByCreatedAtDesc(String player1Puuid, String player2Puuid);

    List<DuoMatchAnalysis> findByPlayer1PuuidAndPlayer2PuuidOrPlayer1PuuidAndPlayer2PuuidOrderByCreatedAtDesc(
            String player1Puuid1, String player2Puuid1, String player1Puuid2, String player2Puuid2);
}
