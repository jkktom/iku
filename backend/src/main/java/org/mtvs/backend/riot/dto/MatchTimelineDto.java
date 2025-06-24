package org.mtvs.backend.riot.dto;

public class MatchTimelineDto {
    private TimelineInfoDto timeInfo;

    public MatchTimelineDto() {
    }

    public TimelineInfoDto getTimeInfo() {
        return timeInfo;
    }

    public void setTimeInfo(TimelineInfoDto timeInfo) {
        this.timeInfo = timeInfo;
    }

    @Override
    public String toString() {
        return "MatchTimelineDto{" +
                "timeInfo=" + timeInfo +
                '}';
    }
}