package org.mtvs.backend.riot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AIAnalysisResponseDto {
    
    private Long analysisId;
    private String puuid;
    private String matchId;
    private String targetPlayerName;
    private String targetChampion;
    private String analysisStatus;
    private String analysisSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // AI 응답의 주요 부분들
    private PerformanceAnalysis performanceAnalysis;
    private List<PlayPhaseAnalysis> phaseAnalyses;
    private List<ImprovementSuggestion> improvements;
    private OverallRating overallRating;
    private String errorMessage;

    // 성능 분석 내부 클래스
    public static class PerformanceAnalysis {
        private String earlyGame;
        private String midGame;
        private String lateGame;
        private String positioning;
        private String teamfight;
        private String objectiveControl;

        // 생성자, getters, setters
        public PerformanceAnalysis() {}

        public String getEarlyGame() { return earlyGame; }
        public void setEarlyGame(String earlyGame) { this.earlyGame = earlyGame; }

        public String getMidGame() { return midGame; }
        public void setMidGame(String midGame) { this.midGame = midGame; }

        public String getLateGame() { return lateGame; }
        public void setLateGame(String lateGame) { this.lateGame = lateGame; }

        public String getPositioning() { return positioning; }
        public void setPositioning(String positioning) { this.positioning = positioning; }

        public String getTeamfight() { return teamfight; }
        public void setTeamfight(String teamfight) { this.teamfight = teamfight; }

        public String getObjectiveControl() { return objectiveControl; }
        public void setObjectiveControl(String objectiveControl) { this.objectiveControl = objectiveControl; }
    }

    // 구간별 분석 내부 클래스
    public static class PlayPhaseAnalysis {
        private String phase; // EARLY, MID, LATE
        private String description;
        private int score; // 1-10점
        private List<String> keyPoints;

        public PlayPhaseAnalysis() {}

        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public List<String> getKeyPoints() { return keyPoints; }
        public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }
    }

    // 개선 제안 내부 클래스
    public static class ImprovementSuggestion {
        private String category; // POSITIONING, TEAMFIGHT, FARMING, etc.
        private String priority; // HIGH, MEDIUM, LOW
        private String description;
        private String actionPlan;

        public ImprovementSuggestion() {}

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getActionPlan() { return actionPlan; }
        public void setActionPlan(String actionPlan) { this.actionPlan = actionPlan; }
    }

    // 전체 평가 내부 클래스
    public static class OverallRating {
        private int totalScore; // 100점 만점
        private String grade; // S, A, B, C, D
        private String summary;
        private Map<String, Integer> categoryScores; // 카테고리별 점수

        public OverallRating() {}

        public int getTotalScore() { return totalScore; }
        public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public Map<String, Integer> getCategoryScores() { return categoryScores; }
        public void setCategoryScores(Map<String, Integer> categoryScores) { this.categoryScores = categoryScores; }
    }

    // 기본 생성자
    public AIAnalysisResponseDto() {}

    // Getters and Setters
    public Long getAnalysisId() { return analysisId; }
    public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }

    public String getPuuid() { return puuid; }
    public void setPuuid(String puuid) { this.puuid = puuid; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getTargetPlayerName() { return targetPlayerName; }
    public void setTargetPlayerName(String targetPlayerName) { this.targetPlayerName = targetPlayerName; }

    public String getTargetChampion() { return targetChampion; }
    public void setTargetChampion(String targetChampion) { this.targetChampion = targetChampion; }

    public String getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(String analysisStatus) { this.analysisStatus = analysisStatus; }

    public String getAnalysisSummary() { return analysisSummary; }
    public void setAnalysisSummary(String analysisSummary) { this.analysisSummary = analysisSummary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public PerformanceAnalysis getPerformanceAnalysis() { return performanceAnalysis; }
    public void setPerformanceAnalysis(PerformanceAnalysis performanceAnalysis) { this.performanceAnalysis = performanceAnalysis; }

    public List<PlayPhaseAnalysis> getPhaseAnalyses() { return phaseAnalyses; }
    public void setPhaseAnalyses(List<PlayPhaseAnalysis> phaseAnalyses) { this.phaseAnalyses = phaseAnalyses; }

    public List<ImprovementSuggestion> getImprovements() { return improvements; }
    public void setImprovements(List<ImprovementSuggestion> improvements) { this.improvements = improvements; }

    public OverallRating getOverallRating() { return overallRating; }
    public void setOverallRating(OverallRating overallRating) { this.overallRating = overallRating; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
