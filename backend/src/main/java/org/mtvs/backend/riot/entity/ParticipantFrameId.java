package org.mtvs.backend.riot.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for ParticipantFrame entity.
 * Combines matchId, timestamp, and participantId to create a unique key for each participant frame.
 */
public class ParticipantFrameId implements Serializable {
    
    private String matchId;
    private long timestamp;
    private byte participantId;  // Participant ID 1-10, perfect for byte range
    
    // Default constructor required by JPA
    public ParticipantFrameId() {}
    
    public ParticipantFrameId(String matchId, long timestamp, byte participantId) {
        this.matchId = matchId;
        this.timestamp = timestamp;
        this.participantId = participantId;
    }
    
    // Getters and Setters
    public String getMatchId() {
        return matchId;
    }
    
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public byte getParticipantId() {
        return participantId;
    }
    
    public void setParticipantId(byte participantId) {
        this.participantId = participantId;
    }
    
    // equals() method required for composite keys
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ParticipantFrameId that = (ParticipantFrameId) obj;
        return timestamp == that.timestamp && 
               participantId == that.participantId &&
               Objects.equals(matchId, that.matchId);
    }
    
    // hashCode() method required for composite keys
    @Override
    public int hashCode() {
        return Objects.hash(matchId, timestamp, participantId);
    }
    
    @Override
    public String toString() {
        return "ParticipantFrameId{" +
                "matchId='" + matchId + '\'' +
                ", timestamp=" + timestamp +
                ", participantId=" + participantId +
                '}';
    }
}