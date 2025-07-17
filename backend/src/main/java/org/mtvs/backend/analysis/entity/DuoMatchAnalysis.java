package org.mtvs.backend.analysis.entity;

import jakarta.persistence.*;
import lombok.Cleanup;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "duo_match_analysis")
public class DuoMatchAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private String matchId;

    @Column(name = "player1_puuid", nullable = false)
    private String player1Puuid;

    @Column(name = "player2_puuid", nullable = false)
    private String player2Puuid;

    @Column(name = "player1_name")
    private String player1Name;

    @Column(name = "player2_name")
    private String player2Name;

    @Column(name = "player1_champion")
    private String player1Champion;

    @Column(name = "player2_champion")
    private String player2Champion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "comparison_result", columnDefinition = "jsonb")
    private Map<String, Object> comparisonResult;

    @Column(name = "analysis_summary", columnDefinition = "text")
    private String analysisSummary;

    @Column(name = "analysis_status")
    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DuoMatchAnalysis() {
        this.createdAt = LocalDateTime.now();
        this.analysisStatus = AnalysisStatus.REQUESTED;
    }

    public DuoMatchAnalysis(Long id, String matchId, String player1Puuid, String player2Puuid, String player1Name, String player2Name, String player1Champion, String player2Champion, Map<String, Object> comparisonResult, String analysisSummary, AnalysisStatus analysisStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.matchId = matchId;
        this.player1Puuid = player1Puuid;
        this.player2Puuid = player2Puuid;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Champion = player1Champion;
        this.player2Champion = player2Champion;
        this.comparisonResult = comparisonResult;
        this.analysisSummary = analysisSummary;
        this.analysisStatus = analysisStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getPlayer1Puuid() {
        return player1Puuid;
    }

    public void setPlayer1Puuid(String player1Puuid) {
        this.player1Puuid = player1Puuid;
    }

    public String getPlayer2Puuid() {
        return player2Puuid;
    }

    public void setPlayer2Puuid(String player2Puuid) {
        this.player2Puuid = player2Puuid;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public String getPlayer1Champion() {
        return player1Champion;
    }

    public void setPlayer1Champion(String player1Champion) {
        this.player1Champion = player1Champion;
    }

    public String getPlayer2Champion() {
        return player2Champion;
    }

    public void setPlayer2Champion(String player2Champion) {
        this.player2Champion = player2Champion;
    }

    public Map<String, Object> getComparisonResult() {
        return comparisonResult;
    }

    public void setComparisonResult(Map<String, Object> comparisonResult) {
        this.comparisonResult = comparisonResult;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(String analysisSummary) {

        this.analysisSummary = analysisSummary;
        this.updatedAt = LocalDateTime.now();
    }

    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
