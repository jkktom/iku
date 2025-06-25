package org.mtvs.backend.riot.dto;

import java.util.List;

public class TimelineInfoDto {
    private long frameInterval;
    private List<FrameDto> frames;
    private long gameId;
    private List<ParticipantDto> participants;

    public TimelineInfoDto() {
    }

    public List<ParticipantDto> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDto> participants) {
        this.participants = participants;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public List<FrameDto> getFrames() {
        return frames;
    }

    public void setFrames(List<FrameDto> frames) {
        this.frames = frames;
    }

    public long getFrameInterval() {
        return frameInterval;
    }

    public void setFrameInterval(long frameInterval) {
        this.frameInterval = frameInterval;
    }
}
