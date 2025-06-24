package org.mtvs.backend.riot.dto;

import java.util.List;
import java.util.Map;

public class FrameDto {
    private long timestamp;
    private Map<String, ParticipantFrameDto> participantFrames;
    private List<EventDto> events;

    public FrameDto() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, ParticipantFrameDto> getParticipantFrames() {
        return participantFrames;
    }

    public void setParticipantFrames(Map<String, ParticipantFrameDto> participantFrames) {
        this.participantFrames = participantFrames;
    }

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "FrameDto{" +
                "timestamp=" + timestamp +
                ", participantFrames=" + participantFrames +
                ", events=" + events +
                '}';
    }
}
