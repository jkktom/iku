package org.mtvs.backend.riot.dto;

import java.util.List;

public class InfoDto {
    private long gameDuration; //게임 총 시간
    private String gameMode; //게임 모드
    private String gameVersion; //게임 버전
    private int queueId; //큐 타입 (ex : 420 = 솔로랭크)
    private List<ParticipantDto> participants; //참가자별 상세 기록 리스트

    public InfoDto() {
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

    public List<ParticipantDto> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDto> participants) {
        this.participants = participants;
    }
}
