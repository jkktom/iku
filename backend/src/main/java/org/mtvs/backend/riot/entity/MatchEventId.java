package org.mtvs.backend.riot.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for MatchEvent entity.
 * Combines matchId, timestamp, and sequenceId to create a unique key for each event.
 */
public class MatchEventId implements Serializable {
    
    private String matchId;
    private long timestamp;
    private short sequenceId;  // Event sequence within the same timestamp
    
    // Default constructor required by JPA
    public MatchEventId() {}
    
    public MatchEventId(String matchId, long timestamp, short sequenceId) {
        this.matchId = matchId;
        this.timestamp = timestamp;
        this.sequenceId = sequenceId;
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
    
    public short getSequenceId() {
        return sequenceId;
    }
    
    public void setSequenceId(short sequenceId) {
        this.sequenceId = sequenceId;
    }
    
    // equals() method required for composite keys
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MatchEventId that = (MatchEventId) obj;
        return timestamp == that.timestamp && 
               sequenceId == that.sequenceId &&
               Objects.equals(matchId, that.matchId);
    }
    
    // hashCode() method required for composite keys
    @Override
    public int hashCode() {
        return Objects.hash(matchId, timestamp, sequenceId);
    }
    
    @Override
    public String toString() {
        return "MatchEventId{" +
                "matchId='" + matchId + '\'' +
                ", timestamp=" + timestamp +
                ", sequenceId=" + sequenceId +
                '}';
    }
}