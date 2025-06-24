package org.mtvs.backend.riot.dto;

import java.util.List;

public class TimelineInfoDto {
    private List<FrameDto> frames;

    public TimelineInfoDto() {
    }

    public List<FrameDto> getFrames() {
        return frames;
    }

    public void setFrames(List<FrameDto> frames) {
        this.frames = frames;
    }

    @Override
    public String toString() {
        return "TimelineInfoDto{" +
                "frames=" + frames +
                '}';
    }
}
