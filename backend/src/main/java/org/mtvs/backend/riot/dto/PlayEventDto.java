package org.mtvs.backend.riot.dto;

import java.util.Map;

public class PlayEventDto {
    private String eventType;
    private int timestamp;
    private String description;
    private Map<String, Object> details;

    public PlayEventDto() {}

    public PlayEventDto(String eventType, int timestamp, String description, Map<String, Object> details) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.description = description;
        this.details = details;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getTimestamp() { return timestamp; }
    public void setTimestamp(int timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
