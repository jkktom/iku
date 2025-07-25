package org.mtvs.backend.analysis.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 모든 분석 엔티티의 공통 필드와 기능을 담은 기본 클래스
 */
@MappedSuperclass
public abstract class BaseAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    
    @Column(name = "puuid", nullable = true, length = 78)
    protected String puuid;
    
    @Column(name = "target_player_name", length = 50)
    protected String targetPlayerName;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_request_data", columnDefinition = "jsonb")
    protected Map<String, Object> aiRequestData;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_response_data", columnDefinition = "jsonb")
    protected Map<String, Object> aiResponseData;
    
    @Column(name = "analysis_summary", columnDefinition = "text")
    protected String analysisSummary;
    
    @Column(name = "analysis_status", length = 20)
    @Enumerated(EnumType.STRING)
    protected AnalysisStatus analysisStatus;
    
    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;
    
    @Column(name = "error_message", columnDefinition = "text")
    protected String errorMessage;

    // 기본 생성자
    public BaseAnalysis() {
        this.createdAt = LocalDateTime.now();
        this.analysisStatus = AnalysisStatus.REQUESTED;
    }

    // 공통 Lifecycle 메서드들
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 공통 Getters and Setters
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

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    public void setTargetPlayerName(String targetPlayerName) {
        this.targetPlayerName = targetPlayerName;
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

    // 공통 유틸리티 메서드들
    public boolean isCompleted() {
        return AnalysisStatus.COMPLETED.equals(this.analysisStatus);
    }

    public boolean isFailed() {
        return AnalysisStatus.FAILED.equals(this.analysisStatus);
    }

    public boolean isProcessing() {
        return AnalysisStatus.PROCESSING.equals(this.analysisStatus);
    }

    public boolean isRequested() {
        return AnalysisStatus.REQUESTED.equals(this.analysisStatus);
    }
}