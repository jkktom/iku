package org.mtvs.backend.riot.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "match_timelines")
@IdClass(MatchTimelineId.class)
public class MatchTimeline {
    
    @Id
    @Column(name = "match_id")
    private String matchId; // Part of composite key - matches Match.matchId

    @Id
    @Column(name = "timestamp")
    private long timestamp; // Part of composite key - timeline timestamp in milliseconds

    // Relationship to Match entity (not part of key but for navigation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", insertable = false, updatable = false)
    private Match match;

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL)
    private List<ParticipantFrame> participantFrames;

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL)
    private List<MatchEvent> events;

    public MatchTimeline() {
    }

    public MatchTimeline(String matchId, long timestamp, Match match, List<ParticipantFrame> participantFrames, List<MatchEvent> events) {
        this.matchId = matchId;
        this.timestamp = timestamp;
        this.match = match;
        this.participantFrames = participantFrames;
        this.events = events;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<ParticipantFrame> getParticipantFrames() {
        return participantFrames;
    }

    public void setParticipantFrames(List<ParticipantFrame> participantFrames) {
        this.participantFrames = participantFrames;
    }

    public List<MatchEvent> getEvents() {
        return events;
    }

    public void setEvents(List<MatchEvent> events) {
        this.events = events;
    }
}