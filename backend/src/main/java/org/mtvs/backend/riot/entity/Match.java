package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    private String matchId;

    private long gameDuration;
    private String gameMode;
    private String gameVersion;
    private int queueId;
    private LocalDateTime gameStartTime;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<MatchTimeline> matchTimelines;

    public Match() {
    }

    public Match(String matchId, long gameDuration, String gameMode, String gameVersion, int queueId, LocalDateTime gameStartTime, List<Participant> participants, List<MatchTimeline> matchTimelines) {
        this.matchId = matchId;
        this.gameDuration = gameDuration;
        this.gameMode = gameMode;
        this.gameVersion = gameVersion;
        this.queueId = queueId;
        this.gameStartTime = gameStartTime;
        this.participants = participants;
        this.matchTimelines = matchTimelines;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public long getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(long gameDuration) {
        this.gameDuration = gameDuration;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public LocalDateTime getGameStartTime() {
        return gameStartTime;
    }

    public void setGameStartTime(LocalDateTime gameStartTime) {
        this.gameStartTime = gameStartTime;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<MatchTimeline> getMatchTimelines() {
        return matchTimelines;
    }

    public void setMatchTimelines(List<MatchTimeline> matchTimelines) {
        this.matchTimelines = matchTimelines;
    }

}
