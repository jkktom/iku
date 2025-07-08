package org.mtvs.backend.multianalysis.entity;

import jakarta.persistence.*;
import org.mtvs.backend.global.entity.BaseEntity;
import org.mtvs.backend.common.constants.AnalysisStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "multi_match_analysis")
public class MultiMatchAnalysis extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "puuid", nullable = false)
    private String puuid;
    
    @Column(name = "game_name", nullable = false)
    private String gameName;
    
    @Column(name = "tag_line", nullable = false)
    private String tagLine;
    
    @Column(name = "match_count", nullable = false)
    private int matchCount;
    
    @Column(name = "match_ids", columnDefinition = "TEXT")
    private String matchIds;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "ai_request", columnDefinition = "JSONB")
    private String aiRequest;
    
    @Column(name = "ai_response", columnDefinition = "JSONB")
    private String aiResponse;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "analysis_duration_seconds")
    private Long analysisDurationSeconds;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public MultiMatchAnalysis() {}
    
    public MultiMatchAnalysis(String puuid, String gameName, String tagLine, int matchCount) {
        this.puuid = puuid;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.matchCount = matchCount;
        this.status = AnalysisStatus.REQUESTED;
    }
    
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
    
    public String getGameName() {
        return gameName;
    }
    
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    
    public String getTagLine() {
        return tagLine;
    }
    
    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }
    
    public int getMatchCount() {
        return matchCount;
    }
    
    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }
    
    public String getMatchIds() {
        return matchIds;
    }
    
    public void setMatchIds(String matchIds) {
        this.matchIds = matchIds;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAiRequest() {
        return aiRequest;
    }
    
    public void setAiRequest(String aiRequest) {
        this.aiRequest = aiRequest;
    }
    
    public String getAiResponse() {
        return aiResponse;
    }
    
    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getAnalysisDurationSeconds() {
        return analysisDurationSeconds;
    }
    
    public void setAnalysisDurationSeconds(Long analysisDurationSeconds) {
        this.analysisDurationSeconds = analysisDurationSeconds;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}