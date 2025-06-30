package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "match_analysis")
public class MatchAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "puuid", nullable = false, length = 78)
    private String puuid;
    
    @Column(name = "match_id", nullable = true, length = 20) // null 허용으로 변경
    private String matchId;
    
    @Column(name = "target_player_name", nullable = true, length = 50) // null 허용
    private String targetPlayerName;
    
    @Column(name = "target_champion", nullable = true, length = 30) // null 허용
    private String targetChampion;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_request_data", columnDefinition = "jsonb")
    private Map<String, Object> aiRequestData;
    
    @JdbcTypeCode(SqlTypes.JSON) 
    @Column(name = "ai_response_data", columnDefinition = "jsonb")
    private Map<String, Object> aiResponseData;
    
    @Column(name = "analysis_summary", columnDefinition = "text")
    private String analysisSummary;
    
    @Column(name = "analysis_status", length = 20)
    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    // 분석 상태 열거형
    public enum AnalysisStatus {
        REQUESTED,    // 분석 요청됨
        PROCESSING,   // 분석 중
        COMPLETED,    // 분석 완료
        FAILED        // 분석 실패
    }

    // 기본 생성자
    public MatchAnalysis() {
        this.createdAt = LocalDateTime.now();
        this.analysisStatus = AnalysisStatus.REQUESTED;
    }

    // 생성자
    public MatchAnalysis(String puuid, String matchId, String targetPlayerName, String targetChampion) {
        this();
        this.puuid = puuid;
        this.matchId = matchId;
        this.targetPlayerName = targetPlayerName;
        this.targetChampion = targetChampion;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    public void setTargetPlayerName(String targetPlayerName) {
        this.targetPlayerName = targetPlayerName;
    }

    public String getTargetChampion() {
        return targetChampion;
    }

    public void setTargetChampion(String targetChampion) {
        this.targetChampion = targetChampion;
    }

    public Map<String, Object> getAiRequestData() {
        return aiRequestData;
    }

    public void setAiRequestData(Map<String, Object> aiRequestData) {
        this.aiRequestData = aiRequestData;
    }

    public Map<String, Object> getAiResponseData() {
        return aiResponseData;
    }

    public void setAiResponseData(Map<String, Object> aiResponseData) {
        this.aiResponseData = aiResponseData;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(String analysisSummary) {
        this.analysisSummary = analysisSummary;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
