package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    private String matchId; //게임 고유 ID

    private long gameDuration; //게임 지속 시간 (초)
    private String gameMode; //게임 모드
    private String gameVersion; //게임 버전
    private int queueId; //게임 타입 큐 ID
    private int mapId; //맵 ID (11=소환사의 협곡, 12=칼바람 나락)

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<MatchTimeline> matchTimelines;

    public Match() {
    }

    public Match(String matchId, long gameDuration, String gameMode, String gameVersion, int queueId, int mapId, List<Participant> participants, List<MatchTimeline> matchTimelines) {
        this.matchId = matchId;
        this.gameDuration = gameDuration;
        this.gameMode = gameMode;
        this.gameVersion = gameVersion;
        this.queueId = queueId;
        this.mapId = mapId;
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

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
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
