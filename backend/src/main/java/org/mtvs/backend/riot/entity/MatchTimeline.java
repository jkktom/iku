package org.mtvs.backend.riot.entity;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import jakarta.persistence.*;
import java.util.List;

@Entity

@Table(name = "match_timelines")
public class MatchTimeline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private long timestamp;

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL)
    private List<ParticipantFrame> participantFrames;

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL)
    private List<MatchEvent> events;

    public MatchTimeline() {
    }

    public MatchTimeline(Long id, Match match, long timestamp, List<ParticipantFrame> participantFrames, List<MatchEvent> events) {
        this.id = id;
        this.match = match;
        this.timestamp = timestamp;
        this.participantFrames = participantFrames;
        this.events = events;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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