package org.mtvs.backend.common.valueobject;

public class PlayerPerformance {
    
    private final int kills;
    private final int deaths;
    private final int assists;
    private final int totalCS;
    private final int goldEarned;
    private final int damageDealt;
    private final int visionScore;
    private final int wardsPlaced;
    
    public PlayerPerformance(int kills, int deaths, int assists, int totalCS, 
                           int goldEarned, int damageDealt, int visionScore, int wardsPlaced) {
        this.kills = Math.max(0, kills);
        this.deaths = Math.max(0, deaths);
        this.assists = Math.max(0, assists);
        this.totalCS = Math.max(0, totalCS);
        this.goldEarned = Math.max(0, goldEarned);
        this.damageDealt = Math.max(0, damageDealt);
        this.visionScore = Math.max(0, visionScore);
        this.wardsPlaced = Math.max(0, wardsPlaced);
    }
    
    public double calculateKDA() {
        if (deaths == 0) {
            return kills + assists;
        }
        return (double) (kills + assists) / deaths;
    }
    
    public boolean isValidKDA() {
        return kills >= 0 && deaths >= 0 && assists >= 0;
    }
    
    public double calculateKillParticipation(int teamKills) {
        if (teamKills == 0) {
            return 0.0;
        }
        return (double) (kills + assists) / teamKills * 100;
    }
    
    public String getPerformanceGrade() {
        double kda = calculateKDA();
        
        if (kda >= 3.0 && damageDealt > 15000) {
            return "S";
        } else if (kda >= 2.0 && damageDealt > 10000) {
            return "A";
        } else if (kda >= 1.0 && damageDealt > 8000) {
            return "B";
        } else if (kda >= 0.5) {
            return "C";
        } else {
            return "D";
        }
    }
    
    public boolean isCarryPerformance() {
        return calculateKDA() >= 2.0 && damageDealt > 15000;
    }
    
    public boolean isSupportPerformance() {
        return assists >= 10 && visionScore >= 30 && wardsPlaced >= 10;
    }
    
    public int getKills() {
        return kills;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public int getAssists() {
        return assists;
    }
    
    public int getTotalCS() {
        return totalCS;
    }
    
    public int getGoldEarned() {
        return goldEarned;
    }
    
    public int getDamageDealt() {
        return damageDealt;
    }
    
    public int getVisionScore() {
        return visionScore;
    }
    
    public int getWardsPlaced() {
        return wardsPlaced;
    }
    
    @Override
    public String toString() {
        return String.format("PlayerPerformance{KDA=%.2f (%d/%d/%d), CS=%d, Gold=%d, Damage=%d, Grade=%s}", 
                           calculateKDA(), kills, deaths, assists, totalCS, goldEarned, damageDealt, getPerformanceGrade());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PlayerPerformance that = (PlayerPerformance) obj;
        return kills == that.kills && deaths == that.deaths && assists == that.assists &&
               totalCS == that.totalCS && goldEarned == that.goldEarned && 
               damageDealt == that.damageDealt && visionScore == that.visionScore &&
               wardsPlaced == that.wardsPlaced;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(kills, deaths, assists, totalCS, goldEarned, damageDealt, visionScore, wardsPlaced);
    }
}