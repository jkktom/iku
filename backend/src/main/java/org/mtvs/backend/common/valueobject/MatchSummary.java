package org.mtvs.backend.common.valueobject;

import java.time.LocalDateTime;

public class MatchSummary {
    
    private final String matchId;
    private final String championName;
    private final boolean isWin;
    private final PlayerPerformance performance;
    private final int gameDuration;
    private final String gameMode;
    private final LocalDateTime gameStartTime;
    
    public MatchSummary(String matchId, String championName, boolean isWin, 
                       PlayerPerformance performance, int gameDuration, 
                       String gameMode, LocalDateTime gameStartTime) {
        this.matchId = matchId;
        this.championName = championName;
        this.isWin = isWin;
        this.performance = performance;
        this.gameDuration = gameDuration;
        this.gameMode = gameMode;
        this.gameStartTime = gameStartTime;
    }
    
    public String getMatchResult() {
        return isWin ? "승리" : "패배";
    }
    
    public String getFormattedDuration() {
        int minutes = gameDuration / 60;
        int seconds = gameDuration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    public String getMatchDescription() {
        return String.format("%s - %s (%d/%d/%d) - %s", 
                           championName, 
                           getMatchResult(),
                           performance.getKills(),
                           performance.getDeaths(), 
                           performance.getAssists(),
                           getFormattedDuration());
    }
    
    public boolean isGoodPerformance() {
        return performance.calculateKDA() >= 2.0 || performance.isCarryPerformance();
    }
    
    public boolean isShortGame() {
        return gameDuration < 900; // Less than 15 minutes
    }
    
    public boolean isLongGame() {
        return gameDuration > 2400; // More than 40 minutes
    }
    
    public String getMatchId() {
        return matchId;
    }
    
    public String getChampionName() {
        return championName;
    }
    
    public boolean isWin() {
        return isWin;
    }
    
    public PlayerPerformance getPerformance() {
        return performance;
    }
    
    public int getGameDuration() {
        return gameDuration;
    }
    
    public String getGameMode() {
        return gameMode;
    }
    
    public LocalDateTime getGameStartTime() {
        return gameStartTime;
    }
    
    @Override
    public String toString() {
        return String.format("MatchSummary{matchId='%s', %s}", matchId, getMatchDescription());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MatchSummary that = (MatchSummary) obj;
        return matchId.equals(that.matchId);
    }
    
    @Override
    public int hashCode() {
        return matchId.hashCode();
    }
}