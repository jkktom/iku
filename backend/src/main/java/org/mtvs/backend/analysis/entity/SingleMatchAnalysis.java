package org.mtvs.backend.analysis.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "single_match_analysis",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"puuid", "match_id"})})
public class SingleMatchAnalysis extends BaseAnalysis {
    
    @Column(name = "match_id", nullable = true, length = 20)
    private String matchId;
    
    @Column(name = "target_champion", length = 30)
    private String targetChampion;
    
    @Column(name = "match_duration")
    private Long matchDuration;
    
    @Column(name = "game_mode", length = 30)
    private String gameMode;

    // 기본 생성자
    public SingleMatchAnalysis() {
        super();
    }

    // 생성자
    public SingleMatchAnalysis(String puuid, String matchId, String targetPlayerName, String targetChampion) {
        super();
        setPuuid(puuid);
        setMatchId(matchId);
        setTargetPlayerName(targetPlayerName);
        setTargetChampion(targetChampion);
    }

    // Specific Getters and Setters (inherited methods are in BaseAnalysis)

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }


    public String getTargetChampion() {
        return targetChampion;
    }

    public void setTargetChampion(String targetChampion) {
        this.targetChampion = targetChampion;
    }

    public Long getMatchDuration() {
        return matchDuration;
    }

    public void setMatchDuration(Long matchDuration) {
        this.matchDuration = matchDuration;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

}