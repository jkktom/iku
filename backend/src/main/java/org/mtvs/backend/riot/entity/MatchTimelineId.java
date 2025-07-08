package org.mtvs.backend.riot.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for MatchTimeline entity.
 * Combines matchId and timestamp to create a natural, meaningful key.
 */
public class MatchTimelineId implements Serializable {
    
    private String matchId;
    private long timestamp;
    
    // Default constructor required by JPA
    public MatchTimelineId() {}
    
    public MatchTimelineId(String matchId, long timestamp) {
        this.matchId = matchId;
        this.timestamp = timestamp;
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
    
    // equals() method required for composite keys
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MatchTimelineId that = (MatchTimelineId) obj;
        return timestamp == that.timestamp && 
               Objects.equals(matchId, that.matchId);
    }
    
    // hashCode() method required for composite keys
    @Override
    public int hashCode() {
        return Objects.hash(matchId, timestamp);
    }
    
    @Override
    public String toString() {
        return "MatchTimelineId{" +
                "matchId='" + matchId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}