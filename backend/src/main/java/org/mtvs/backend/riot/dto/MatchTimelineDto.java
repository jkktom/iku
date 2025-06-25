package org.mtvs.backend.riot.dto;

public class MatchTimelineDto {
    private TimelineInfoDto info;

    public MatchTimelineDto() {
    }

    public TimelineInfoDto getInfo() {
        return info;
    }

    public void setInfo(TimelineInfoDto info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "MatchTimelineDto{" +
                "info=" + info +
                '}';
    }
}